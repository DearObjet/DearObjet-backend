package app.dearobjet.backend.domain.shop.dto;

import app.dearobjet.backend.domain.shop.entity.Shop;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ShopDetailResponse(
        Long userId,
        String shopName,
        String phoneNumber,
        String businessAddress,
        ShopBusinessHoursResponse businessHours,
        @JsonProperty("isFavorite")
        boolean favorite
) {
    public static ShopDetailResponse from(Shop shop, ShopBusinessHoursResponse businessHours, boolean favorite) {
        return new ShopDetailResponse(
                shop.getUser() == null ? null : shop.getUser().getId(),
                shop.getShopName(),
                shop.getUser() == null ? null : shop.getUser().getPhoneNumber(),
                shop.getBusinessAddress(),
                businessHours,
                favorite
        );
    }
}
