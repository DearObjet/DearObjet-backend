package app.dearobjet.backend.domain.payment.dto;

import app.dearobjet.backend.domain.payment.entity.PaymentProvider;
import app.dearobjet.backend.domain.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
public class PaymentResponse {
    private final Long paymentId;
    private final Long orderId;
    private final String orderNo;
    private final PaymentProvider provider;
    private final PaymentStatus status;
    private final Long amount;
    private final String paymentKey;
    private final OffsetDateTime approvedAt;
    private final OffsetDateTime canceledAt;
    private final String failReason;
}