package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.dto.projection.ShopSuggestionRow;
import app.dearobjet.backend.domain.user.enums.Specialty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ShopSuggestionItemResponse {

    private final Long shopId;
    private final Long userId;
    private final String shopName;
    private final String shopImageUrl;
    private final Specialty specialty;
    private final String instagramId;

    public static ShopSuggestionItemResponse from(ShopSuggestionRow row) {
        return new ShopSuggestionItemResponse(
                row.getShopId(),
                row.getUserId(),
                row.getShopName(),
                row.getShopImageUrl(),
                row.getSpecialty(),
                row.getInstagramId()
        );
    }
}
