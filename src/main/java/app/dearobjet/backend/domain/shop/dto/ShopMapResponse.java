package app.dearobjet.backend.domain.shop.dto;

import java.util.List;

public record ShopMapResponse(
        List<ShopMapItemResponse> shops
) {
}
