package app.dearobjet.backend.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    private Buyer buyer;
    private List<ItemLine> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Buyer {
        private String name;
        private String phone;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemLine {
        private Long itemId;
        private Integer quantity;
        private Long unitPrice;
    }
}
