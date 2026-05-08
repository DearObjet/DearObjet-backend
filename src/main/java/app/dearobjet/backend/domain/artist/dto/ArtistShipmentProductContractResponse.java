package app.dearobjet.backend.domain.artist.dto;

import app.dearobjet.backend.domain.contract.dto.projection.ArtistShipmentProductRow;
import app.dearobjet.backend.domain.contract.entity.ShopArtistContract;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ArtistShipmentProductContractResponse {

    private final Long contractId;
    private final ContractStatus contractStatus;
    private final LocalDate contractStartDate;
    private final LocalDate contractEndDate;
    private final List<ArtistShipmentProductItemResponse> items;

    public static ArtistShipmentProductContractResponse from(
            ShopArtistContract contract,
            List<ArtistShipmentProductRow> rows
    ) {
        return new ArtistShipmentProductContractResponse(
                contract.getShopArtistContractsId(),
                contract.getContractStatus(),
                contract.getContractStartDate(),
                contract.getContractEndDate(),
                rows.stream()
                        .map(ArtistShipmentProductItemResponse::from)
                        .toList()
        );
    }
}
