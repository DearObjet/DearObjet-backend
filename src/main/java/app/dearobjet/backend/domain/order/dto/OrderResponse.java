package app.dearobjet.backend.domain.order.dto;

import app.dearobjet.backend.domain.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class OrderResponse {
    private final Long orderId;
    private final String orderNo;
    private final OrderStatus status;
    private final Long totalAmount;

    private final String buyerName;
    private final String buyerPhone;

    private final String receiverName;
    private final String receiverPhone;

    private final String carrier;
    private final String trackingNo;

    private final OffsetDateTime paidAt;
    private final OffsetDateTime shippedAt;
    private final OffsetDateTime deliveredAt;
    private final OffsetDateTime completedAt;
    private final OffsetDateTime cancelledAt;

    private final String cancelReason;

    private final List<OrderItemResponse> items;
}
