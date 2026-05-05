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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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
        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        Order order = payment.getOrder();
        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            throw new OrderAccessDeniedException(order.getOrdersId());
        }

        if (payment.getStatus() == PaymentStatus.DONE) return;
        if (payment.isTerminal()) {
            throw new PaymentBadRequestException("이미 처리 완료된 결제입니다. status=" + payment.getStatus());
        }

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

        Payment payment = paymentRepository.findByPaymentKeyForUpdate(paymentKey).orElse(null);
        if (payment == null) return;

        if (payment.isTerminal()) return;

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
        } else if ("CANCELED".equals(status)) {
            payment.markCanceled();
            payment.getOrder().cancel("WEBHOOK_CANCELED");
        } else if ("ABORTED".equals(status) || "EXPIRED".equals(status)) {
            payment.markFailed(status);
        }
    }

    @Transactional(readOnly = true)
    public PaymentListResponse getPayments(Long userId, LocalDate from, LocalDate to, int page, int size) {
        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = LocalDateTime.of(to, LocalTime.of(23, 59, 59));

        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                size,
                Sort.by(Sort.Direction.DESC, "paymentId")
        );

        Page<Payment> paymentPage = paymentRepository.findByOrder_User_IdAndCreatedAtBetween(
                userId, fromDt, toDt, pageable
        );

        List<PaymentResponse> items = new ArrayList<>();
        for (Payment payment : paymentPage.getContent()) {
            items.add(toResponse(payment));
        }

        return new PaymentListResponse(items, page, paymentPage.getTotalPages());
    }

    private PaymentResponse toResponse(Payment payment) {
        Order order = payment.getOrder();

        return new PaymentResponse(
                payment.getPaymentId(),
                order.getOrdersId(),
                order.getOrderNumber(),
                payment.getProvider(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getPaymentKey(),
                payment.getApprovedAt(),
                payment.getCanceledAt(),
                payment.getFailReason()
        );
    }
}