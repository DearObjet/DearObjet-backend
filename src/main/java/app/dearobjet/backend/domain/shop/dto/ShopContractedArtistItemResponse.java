package app.dearobjet.backend.domain.shop.dto;

import app.dearobjet.backend.domain.contract.dto.projection.ShopContractedArtistRow;

public record ShopContractedArtistItemResponse(
        Long artistId,
        String artistName,
        String artistImageUrl
) {
    public static ShopContractedArtistItemResponse from(ShopContractedArtistRow row) {
        return new ShopContractedArtistItemResponse(
                row.getArtistId(),
                row.getArtistName(),
                row.getArtistImageUrl()
        );
    }
}
