package app.dearobjet.backend.domain.payment.entity;

import app.dearobjet.backend.domain.order.entity.Order;
import app.dearobjet.backend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orders_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PaymentProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PaymentStatus status;

    @Column(nullable = false)
    private Long amount;

    @Column(length = 200)
    private String paymentKey; // 토스 paymentKey

    private OffsetDateTime approvedAt;
    private OffsetDateTime canceledAt;

    @Column(length = 100)
    private String failReason;

    public boolean isTerminal() {
        return this.status == PaymentStatus.DONE
                || this.status == PaymentStatus.CANCELED
                || this.status == PaymentStatus.FAILED;
    }

    public void markDone(String paymentKey) {
        this.status = PaymentStatus.DONE;
        this.paymentKey = paymentKey;
        this.approvedAt = OffsetDateTime.now();
    }

    public void markCanceled() {
        this.status = PaymentStatus.CANCELED;
        this.canceledAt = OffsetDateTime.now();
    }

    public void markFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failReason = reason;
    }
}