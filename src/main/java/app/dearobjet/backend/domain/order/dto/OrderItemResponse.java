package app.dearobjet.backend.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderItemResponse {
    private final Long itemId;
    private final Integer quantity;
    private final Long unitPrice;
    private final Long lineAmount;
}
