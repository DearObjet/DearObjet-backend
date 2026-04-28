package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.dto.projection.ContractInventoryProductRow;
import app.dearobjet.backend.domain.contract.entity.ShopArtistContract;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContractInventoryDetailResponse {

    private final Long contractId;
    private final String artistName;
    private final String memo;
    private final List<ContractInventoryProductResponse> items;

    public static ContractInventoryDetailResponse from(
            ShopArtistContract contract,
            List<ContractInventoryProductRow> rows
    ) {
        return new ContractInventoryDetailResponse(
                contract.getShopArtistContractsId(),
                contract.getArtist().getBusinessProfile().getBusinessName(),
                contract.getMemo() == null ? "" : contract.getMemo(),
                rows.stream()
                        .map(ContractInventoryProductResponse::from)
                        .toList()
        );
    }
}
