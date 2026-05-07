package app.dearobjet.backend.domain.artist.dto;

import app.dearobjet.backend.domain.contract.dto.projection.ArtistShipmentShopRow;
import app.dearobjet.backend.domain.user.enums.Specialty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ArtistShipmentShopItemResponse {

    private final Long contractId;
    private final Long shopId;
    private final String shopName;
    private final Specialty specialty;
    private final LocalDate contractStartDate;
    private final LocalDate contractEndDate;
    private final String inboundStatusCode;
    private final String inboundStatusLabel;

    public static ArtistShipmentShopItemResponse from(ArtistShipmentShopRow row) {
        ArtistShipmentInboundStatus inboundStatus = ArtistShipmentInboundStatus.from(
                row.getRecentStockedAt(),
                row.getInboundConfirmed()
        );

        return new ArtistShipmentShopItemResponse(
                row.getContractId(),
                row.getShopId(),
                row.getShopName(),
                row.getSpecialty(),
                row.getContractStartDate(),
                row.getContractEndDate(),
                inboundStatus.name(),
                inboundStatus.getLabel()
        );
    }
}
