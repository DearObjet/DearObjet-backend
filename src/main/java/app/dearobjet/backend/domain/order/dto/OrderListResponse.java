package app.dearobjet.backend.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OrderListResponse {
    private final List<OrderResponse> items;
    private final int page;
    private final int totalPages;
}
