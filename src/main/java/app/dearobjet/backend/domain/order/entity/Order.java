package app.dearobjet.backend.domain.order.entity;

import app.dearobjet.backend.domain.shop.entity.Shop;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orders_id")
    private Long ordersId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "order_number", unique = true)
    private String orderNumber;

    @Column(name = "total_amount")
    private Double totalAmount;

    private String status;  // ENUM

    @Column(name = "receiver_name")
    private String receiverName;

    @Column(name = "receiver_phone")
    private String receiverPhone;

    @Column(name = "ordered_at")
    private java.time.LocalDateTime orderedAt;

    @Column(name = "paid_at")
    private java.time.LocalDateTime paidAt;

    @Column(name = "shipped_at")
    private java.time.LocalDateTime shippedAt;

    @Column(name = "completed_at")
    private java.time.LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private java.time.LocalDateTime cancelledAt;
}