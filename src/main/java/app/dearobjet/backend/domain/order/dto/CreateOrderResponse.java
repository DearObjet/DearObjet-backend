package app.dearobjet.backend.domain.order.dto;

import app.dearobjet.backend.domain.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateOrderResponse {
    private final Long orderId;
    private final String orderNo;
    private final OrderStatus status;
}
