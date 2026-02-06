package app.dearobjet.backend.domain.order.entity;

import app.dearobjet.backend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orders_id", nullable = false)
    private Order order;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private Integer quantity;

    // 주문 당시 단가
    @Column(name = "unit_price", nullable = false)
    private Long unitPrice;

    // 단가 * 주문 수량
    @Column(name = "line_amount", nullable = false)
    private Long lineAmount;

    void setOrder(Order order) {
        this.order = order;
    }
}
