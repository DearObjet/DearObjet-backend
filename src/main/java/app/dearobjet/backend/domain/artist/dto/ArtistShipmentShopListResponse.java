package app.dearobjet.backend.domain.artist.dto;

import app.dearobjet.backend.domain.contract.dto.projection.ArtistShipmentShopRow;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ArtistShipmentShopListResponse {

    private final List<ArtistShipmentShopItemResponse> items;

    public static ArtistShipmentShopListResponse from(List<ArtistShipmentShopRow> rows) {
        return new ArtistShipmentShopListResponse(
                rows.stream()
                        .map(ArtistShipmentShopItemResponse::from)
                        .toList()
        );
    }
}
