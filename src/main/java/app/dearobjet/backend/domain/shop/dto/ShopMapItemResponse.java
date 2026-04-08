package app.dearobjet.backend.domain.shop.dto;

import lombok.Builder;

@Builder
public record ShopMapItemResponse(
        Long shopId,
        String shopName,
        String businessName,
        String businessAddress,
        double latitude,
        double longitude
) {
}
