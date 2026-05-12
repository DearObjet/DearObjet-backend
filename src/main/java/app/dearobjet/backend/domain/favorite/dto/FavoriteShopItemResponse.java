package app.dearobjet.backend.domain.favorite.dto;

import app.dearobjet.backend.domain.favorite.entity.Favorite;
import app.dearobjet.backend.domain.shop.entity.Shop;
import java.time.LocalDateTime;

public record FavoriteShopItemResponse(
        Long favoriteId,
        Long shopId,
        String shopName,
        String profileUrl,
        String businessAddress,
        String phoneNumber,
        String instagramId,
        Double latitude,
        Double longitude,
        LocalDateTime favoritedAt
) {
    public static FavoriteShopItemResponse from(Favorite favorite, Shop shop) {
        return new FavoriteShopItemResponse(
                favorite.getFavoriteId(),
                shop.getShopId(),
                shop.getShopName(),
                shop.getUser() == null ? null : shop.getUser().getProfileUrl(),
                shop.getBusinessAddress(),
                shop.getUser() == null ? null : shop.getUser().getPhoneNumber(),
                shop.getInstagramId(),
                shop.getLatitude(),
                shop.getLongitude(),
                favorite.getCreatedAt()
        );
    }
}
