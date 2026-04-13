package app.dearobjet.backend.domain.shop.dto;

public record ShopGeocodeResponse(
        int totalCount,
        int updatedCount,
        int skippedCount,
        int failedCount
) {
}
