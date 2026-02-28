package app.dearobjet.backend.domain.payment;

import app.dearobjet.backend.domain.order.OrderAccessDeniedException;
import app.dearobjet.backend.domain.order.OrderNotFoundException;
import app.dearobjet.backend.domain.order.OrderRepository;
import app.dearobjet.backend.domain.order.entity.Order;
import app.dearobjet.backend.domain.order.entity.OrderStatus;
import app.dearobjet.backend.domain.payment.dto.*;
import app.dearobjet.backend.domain.payment.entity.*;
import app.dearobjet.backend.domain.payment.toss.TossPaymentsClient;
import app.dearobjet.backend.domain.payment.toss.TossPaymentsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    private final TossPaymentsClient tossClient;
    private final TossPaymentsProperties tossProps;

    @Transactional
    public CreatePaymentResponse createPayment(Long userId, CreatePaymentRequest req) {
        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(req.getOrderId()));

        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            throw new OrderAccessDeniedException(order.getOrdersId());
        }

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new PaymentBadRequestException("결제 가능한 주문 상태가 아닙니다. status=" + order.getStatus());
        }

        if (!order.getTotalAmount().equals(req.getAmount())) {
            throw new PaymentBadRequestException("결제 금액이 주문 금액과 다릅니다.");
        }

        Payment payment = Payment.builder()
                .order(order)
                .provider(req.getProvider() == null ? PaymentProvider.TOSS : req.getProvider())
                .status(PaymentStatus.READY)
                .amount(req.getAmount())
                .build();

        Payment saved = paymentRepository.save(payment);

        return new CreatePaymentResponse(
                saved.getPaymentId(),
                order.getOrderNumber(),
                saved.getAmount(),
                tossProps.getClientKey()
        );
    }

    @Transactional
    public void confirmPayment(Long userId, Long paymentId, ConfirmPaymentRequest req) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        Order order = payment.getOrder();
        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            throw new OrderAccessDeniedException(order.getOrdersId());
        }

        if (payment.getStatus() == PaymentStatus.DONE) return;

        if (!order.getOrderNumber().equals(req.getOrderId())) {
            throw new PaymentBadRequestException("orderId가 주문번호와 다릅니다.");
        }

        if (!payment.getAmount().equals(req.getPaidAmount())) {
            throw new PaymentBadRequestException("승인 금액이 결제 금액과 다릅니다.");
        }

        try {
            tossClient.confirm(req.getPaymentKey(), req.getOrderId(), req.getPaidAmount());
            payment.markDone(req.getPaymentKey());
            order.updateStatus(OrderStatus.PAID);
        } catch (Exception e) {
            payment.markFailed("CONFIRM_FAILED");
            throw e;
        }
    }

    @Transactional
    public void cancelPayment(Long userId, Long paymentId, CancelPaymentRequest req) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        Order order = payment.getOrder();
        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            throw new OrderAccessDeniedException(order.getOrdersId());
        }

        if (payment.getStatus() != PaymentStatus.DONE) {
            throw new PaymentBadRequestException("취소 가능한 결제 상태가 아닙니다. status=" + payment.getStatus());
        }

        Long cancelAmount = (req.getAmount() == null) ? payment.getAmount() : req.getAmount();

        tossClient.cancel(payment.getPaymentKey(), cancelAmount, req.getReason());
        payment.markCanceled();
        order.cancel(req.getReason() == null ? "USER_CANCEL" : req.getReason());
    }

    @Transactional
    public void handleWebhookToss(String paymentKey) {
        if (paymentKey == null || paymentKey.isBlank()) return;

        Payment payment = paymentRepository.findByPaymentKey(paymentKey).orElse(null);
        if (payment == null) return;

        if (payment.getStatus() == PaymentStatus.DONE) return;

        TossPaymentResponse tossPayment = tossClient.getPayment(paymentKey);
        String status = tossPayment == null ? null : tossPayment.getStatus();
        String orderId = tossPayment == null ? null : tossPayment.getOrderId();
        Long totalAmount = tossPayment == null ? null : tossPayment.getTotalAmount();

        if (status == null || orderId == null || totalAmount == null) return;

        if (!payment.getOrder().getOrderNumber().equals(orderId)) return;
        if (!payment.getAmount().equals(totalAmount)) return;

        if ("DONE".equals(status)) {
            payment.markDone(paymentKey);
            payment.getOrder().updateStatus(OrderStatus.PAID);
        }
    }
}