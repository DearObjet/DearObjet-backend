package app.dearobjet.backend.domain.order.entity;

import app.dearobjet.backend.domain.shop.entity.Shop;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private Shop shop; // 소속 소품샵 없으면 null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "order_number", unique = true, nullable = false, length = 40)
    private String orderNumber;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "buyer_name", nullable = false, length = 50)
    private String buyerName;

    @Column(name = "buyer_phone", nullable = false, length = 20)
    private String buyerPhone;

    @Column(name = "receiver_name", length = 50)
    private String receiverName;

    @Column(name = "receiver_phone", length = 20)
    private String receiverPhone;

    // 배송 정보
    @Column(name = "carrier", length = 30)
    private String carrier;

    @Column(name = "tracking_no", length = 50)
    private String trackingNo;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    @Column(name = "shipped_at")
    private OffsetDateTime shippedAt;

    @Column(name = "delivered_at")
    private OffsetDateTime deliveredAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @Column(name = "cancel_reason", length = 50)
    private String cancelReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void updateTotalAmount(long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void updateStatus(OrderStatus status) {
        this.status = status;

        OffsetDateTime now = OffsetDateTime.now();
        if (status == OrderStatus.PAID) this.paidAt = now;
        if (status == OrderStatus.SHIPPING) this.shippedAt = now;
        if (status == OrderStatus.DELIVERED) this.deliveredAt = now;
        if (status == OrderStatus.COMPLETED) this.completedAt = now;
        if (status == OrderStatus.CANCELED) this.cancelledAt = now;
    }

    public void cancel(String reason) {
        this.status = OrderStatus.CANCELED;
        this.cancelReason = reason;
        this.cancelledAt = OffsetDateTime.now();
    }

    public void updateShipping(String carrier, String trackingNo) {
        this.carrier = carrier;
        this.trackingNo = trackingNo;
    }
}
