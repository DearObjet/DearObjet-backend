package app.dearobjet.backend.domain.artist.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateArtistShipmentResponse {

    private final Long shopId;
    private final Long contractId;
    private final List<CreateArtistShipmentItemResponse> items;

    public static CreateArtistShipmentResponse from(
            Long shopId,
            Long contractId,
            List<CreateArtistShipmentItemResponse> items
    ) {
        return new CreateArtistShipmentResponse(shopId, contractId, items);
    }
}
