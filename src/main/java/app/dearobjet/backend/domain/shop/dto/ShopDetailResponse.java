package app.dearobjet.backend.domain.shop.dto;

import app.dearobjet.backend.domain.shop.entity.Shop;

public record ShopDetailResponse(
        String shopName,
        String phoneNumber,
        String businessAddress
) {
    public static ShopDetailResponse from(Shop shop) {
        return new ShopDetailResponse(
                shop.getShopName(),
                shop.getUser() == null ? null : shop.getUser().getPhoneNumber(),
                shop.getBusinessAddress()
        );
    }
}
