package app.dearobjet.backend.domain.payment.entity;

import app.dearobjet.backend.domain.classes.ClassReservation;
import app.dearobjet.backend.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payments_id")
    private Long paymentsId;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_amount")
    private Double paymentAmount;

    @Column(name = "pg_id")
    private String pgId;

    @Column(name = "approved_at")
    private java.time.LocalDateTime approvedAt;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @Column(name = "last_modified_at")
    private java.time.LocalDateTime lastModifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private ClassReservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orders_id")
    private Order order;
}
