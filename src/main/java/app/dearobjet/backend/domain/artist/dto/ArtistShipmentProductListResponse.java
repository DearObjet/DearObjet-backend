package app.dearobjet.backend.domain.artist.dto;

import app.dearobjet.backend.domain.contract.dto.projection.ArtistShipmentProductRow;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ArtistShipmentProductListResponse {

    private final Long contractId;
    private final List<ArtistShipmentProductItemResponse> items;

    public static ArtistShipmentProductListResponse from(Long contractId, List<ArtistShipmentProductRow> rows) {
        return new ArtistShipmentProductListResponse(
                contractId,
                rows.stream()
                        .map(ArtistShipmentProductItemResponse::from)
                        .toList()
        );
    }
}
