package app.dearobjet.backend.domain.artist.dto;

import app.dearobjet.backend.domain.contract.dto.projection.ArtistShipmentProductRow;
import app.dearobjet.backend.domain.contract.entity.ShopArtistContract;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ArtistShipmentProductListResponse {

    private final Long shopId;
    private final List<ArtistShipmentProductContractResponse> contracts;

    public static ArtistShipmentProductListResponse from(
            Long shopId,
            List<ShopArtistContract> contracts,
            List<ArtistShipmentProductRow> rows
    ) {
        Map<Long, List<ArtistShipmentProductRow>> rowsByContractId = rows.stream()
                .collect(Collectors.groupingBy(ArtistShipmentProductRow::getContractId));

        return new ArtistShipmentProductListResponse(
                shopId,
                contracts.stream()
                        .map(contract -> ArtistShipmentProductContractResponse.from(
                                contract,
                                rowsByContractId.getOrDefault(
                                        contract.getShopArtistContractsId(),
                                        Collections.emptyList()
                                )
                        ))
                        .toList()
        );
    }
}
