package app.dearobjet.backend.domain.shop.dto;

import app.dearobjet.backend.domain.contract.dto.projection.ShopContractedArtistRow;
import java.util.List;

public record ShopContractedArtistListResponse(
        List<ShopContractedArtistItemResponse> items
) {
    public static ShopContractedArtistListResponse from(List<ShopContractedArtistRow> rows) {
        return new ShopContractedArtistListResponse(
                rows.stream()
                        .map(ShopContractedArtistItemResponse::from)
                        .toList()
        );
    }
}
