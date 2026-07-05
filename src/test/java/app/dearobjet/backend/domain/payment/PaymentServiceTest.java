package app.dearobjet.backend.domain.payment;

import app.dearobjet.backend.domain.order.OrderAccessDeniedException;
import app.dearobjet.backend.domain.order.OrderRepository;
import app.dearobjet.backend.domain.order.entity.Order;
import app.dearobjet.backend.domain.order.entity.OrderStatus;
import app.dearobjet.backend.domain.payment.dto.CancelPaymentRequest;
import app.dearobjet.backend.domain.payment.dto.ConfirmPaymentRequest;
import app.dearobjet.backend.domain.payment.dto.CreatePaymentRequest;
import app.dearobjet.backend.domain.payment.dto.CreatePaymentResponse;
import app.dearobjet.backend.domain.payment.entity.Payment;
import app.dearobjet.backend.domain.payment.entity.PaymentProvider;
import app.dearobjet.backend.domain.payment.entity.PaymentStatus;
import app.dearobjet.backend.domain.payment.toss.TossPaymentsClient;
import app.dearobjet.backend.domain.payment.toss.TossPaymentsProperties;
import app.dearobjet.backend.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DisplayName("PaymentService 결제 승인·취소 흐름 테스트")
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long ORDER_ID = 10L;
    private static final Long PAYMENT_ID = 100L;
    private static final String ORDER_NO = "ORD-20260705-123456";
    private static final String PAYMENT_KEY = "toss_payment_key_abc";
    private static final Long AMOUNT = 10_000L;

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TossPaymentsClient tossClient;

    @Mock
    private TossPaymentsProperties tossProps;

    @Test
    @DisplayName("금액 일치하면 결제 생성 (READY)")
    void givenMatchingAmount_whenCreatePayment_thenSaveReadyPayment() {
        Order order = order(ORDER_ID, user(USER_ID), ORDER_NO, AMOUNT, OrderStatus.CREATED);
        given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(order));
        given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(tossProps.getClientKey()).willReturn("test_ck");

        CreatePaymentResponse response =
                paymentService.createPayment(USER_ID, new CreatePaymentRequest(ORDER_ID, null, AMOUNT));

        assertThat(response.getOrderNo()).isEqualTo(ORDER_NO);
        assertThat(response.getAmount()).isEqualTo(AMOUNT);
        assertThat(response.getClientKey()).isEqualTo("test_ck");

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(PaymentStatus.READY);
        assertThat(captor.getValue().getProvider()).isEqualTo(PaymentProvider.TOSS);
        assertThat(captor.getValue().getAmount()).isEqualTo(AMOUNT);
    }

    @Test
    @DisplayName("금액 다르면 결제 생성 실패")
    void givenTamperedAmount_whenCreatePayment_thenThrowAndNotSave() {
        Order order = order(ORDER_ID, user(USER_ID), ORDER_NO, AMOUNT, OrderStatus.CREATED);
        given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(order));

        assertThatThrownBy(() ->
                paymentService.createPayment(USER_ID, new CreatePaymentRequest(ORDER_ID, null, 1L)))
                .isInstanceOf(PaymentBadRequestException.class);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("승인 성공 → 결제 DONE, 주문 PAID")
    void givenReadyPayment_whenConfirm_thenMarkDoneAndOrderPaid() {
        Order order = order(ORDER_ID, user(USER_ID), ORDER_NO, AMOUNT, OrderStatus.CREATED);
        Payment payment = payment(PAYMENT_ID, order, AMOUNT, PaymentStatus.READY);
        given(paymentRepository.findByIdForUpdate(PAYMENT_ID)).willReturn(Optional.of(payment));

        paymentService.confirmPayment(USER_ID, PAYMENT_ID,
                new ConfirmPaymentRequest(PAYMENT_KEY, ORDER_NO, AMOUNT));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.DONE);
        assertThat(payment.getPaymentKey()).isEqualTo(PAYMENT_KEY);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        verify(tossClient).confirm(PAYMENT_KEY, ORDER_NO, AMOUNT);
    }

    @Test
    @DisplayName("승인 금액 위변조 시 토스 호출 없이 거부")
    void givenTamperedAmount_whenConfirm_thenThrowAndNotCallToss() {
        Order order = order(ORDER_ID, user(USER_ID), ORDER_NO, AMOUNT, OrderStatus.CREATED);
        Payment payment = payment(PAYMENT_ID, order, AMOUNT, PaymentStatus.READY);
        given(paymentRepository.findByIdForUpdate(PAYMENT_ID)).willReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.confirmPayment(USER_ID, PAYMENT_ID,
                new ConfirmPaymentRequest(PAYMENT_KEY, ORDER_NO, 1L)))
                .isInstanceOf(PaymentBadRequestException.class);

        verify(tossClient, never()).confirm(any(), any(), any());
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.READY);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    @DisplayName("이미 DONE이면 재승인 무시 (멱등)")
    void givenAlreadyDone_whenConfirm_thenSkipTossCall() {
        Order order = order(ORDER_ID, user(USER_ID), ORDER_NO, AMOUNT, OrderStatus.PAID);
        Payment payment = payment(PAYMENT_ID, order, AMOUNT, PaymentStatus.DONE);
        given(paymentRepository.findByIdForUpdate(PAYMENT_ID)).willReturn(Optional.of(payment));

        paymentService.confirmPayment(USER_ID, PAYMENT_ID,
                new ConfirmPaymentRequest(PAYMENT_KEY, ORDER_NO, AMOUNT));

        verify(tossClient, never()).confirm(any(), any(), any());
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.DONE);
    }

    @Test
    @DisplayName("승인 실패 시 FAILED로 저장, 주문 상태 유지")
    void givenTossFailure_whenConfirm_thenMarkFailedAndKeepOrder() {
        Order order = order(ORDER_ID, user(USER_ID), ORDER_NO, AMOUNT, OrderStatus.CREATED);
        Payment payment = payment(PAYMENT_ID, order, AMOUNT, PaymentStatus.READY);
        given(paymentRepository.findByIdForUpdate(PAYMENT_ID)).willReturn(Optional.of(payment));
        given(tossClient.confirm(anyString(), anyString(), anyLong()))
                .willThrow(new RuntimeException("toss down"));

        assertThatThrownBy(() -> paymentService.confirmPayment(USER_ID, PAYMENT_ID,
                new ConfirmPaymentRequest(PAYMENT_KEY, ORDER_NO, AMOUNT)))
                .isInstanceOf(PaymentConfirmException.class);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.getFailReason()).isEqualTo("CONFIRM_FAILED");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    @DisplayName("남의 결제는 승인 불가")
    void givenOtherUsersPayment_whenConfirm_thenThrowAccessDenied() {
        Order order = order(ORDER_ID, user(OTHER_USER_ID), ORDER_NO, AMOUNT, OrderStatus.CREATED);
        Payment payment = payment(PAYMENT_ID, order, AMOUNT, PaymentStatus.READY);
        given(paymentRepository.findByIdForUpdate(PAYMENT_ID)).willReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.confirmPayment(USER_ID, PAYMENT_ID,
                new ConfirmPaymentRequest(PAYMENT_KEY, ORDER_NO, AMOUNT)))
                .isInstanceOf(OrderAccessDeniedException.class);

        verify(tossClient, never()).confirm(any(), any(), any());
    }

    @Test
    @DisplayName("DONE 결제 취소 → 결제·주문 모두 취소")
    void givenDonePayment_whenCancel_thenMarkCanceledAndOrderCanceled() {
        Order order = order(ORDER_ID, user(USER_ID), ORDER_NO, AMOUNT, OrderStatus.PAID);
        Payment payment = Payment.builder()
                .paymentId(PAYMENT_ID)
                .order(order)
                .provider(PaymentProvider.TOSS)
                .status(PaymentStatus.DONE)
                .amount(AMOUNT)
                .paymentKey(PAYMENT_KEY)
                .build();
        given(paymentRepository.findByIdForUpdate(PAYMENT_ID)).willReturn(Optional.of(payment));

        paymentService.cancelPayment(USER_ID, PAYMENT_ID, new CancelPaymentRequest(null, "단순 변심"));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(order.getCancelReason()).isEqualTo("단순 변심");
        verify(tossClient).cancel(PAYMENT_KEY, AMOUNT, "단순 변심");
    }

    @Test
    @DisplayName("DONE 아니면 취소 불가")
    void givenNonDonePayment_whenCancel_thenThrowAndNotCallToss() {
        Order order = order(ORDER_ID, user(USER_ID), ORDER_NO, AMOUNT, OrderStatus.CREATED);
        Payment payment = payment(PAYMENT_ID, order, AMOUNT, PaymentStatus.READY);
        given(paymentRepository.findByIdForUpdate(PAYMENT_ID)).willReturn(Optional.of(payment));

        assertThatThrownBy(() ->
                paymentService.cancelPayment(USER_ID, PAYMENT_ID, new CancelPaymentRequest(null, "취소")))
                .isInstanceOf(PaymentBadRequestException.class);

        verify(tossClient, never()).cancel(any(), anyLong(), any());
    }

    private User user(Long id) {
        return User.builder().id(id).build();
    }

    private Order order(Long id, User user, String orderNo, Long amount, OrderStatus status) {
        return Order.builder()
                .ordersId(id)
                .user(user)
                .orderNumber(orderNo)
                .totalAmount(amount)
                .status(status)
                .build();
    }

    private Payment payment(Long id, Order order, Long amount, PaymentStatus status) {
        return Payment.builder()
                .paymentId(id)
                .order(order)
                .provider(PaymentProvider.TOSS)
                .status(status)
                .amount(amount)
                .build();
    }
}
