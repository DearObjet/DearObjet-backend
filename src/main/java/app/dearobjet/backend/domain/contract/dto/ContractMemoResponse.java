package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.entity.ShopArtistContract;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContractMemoResponse {

    private final Long contractId;
    private final String memo;

    public static ContractMemoResponse from(ShopArtistContract contract) {
        return new ContractMemoResponse(
                contract.getShopArtistContractsId(),
                contract.getMemo() == null ? "" : contract.getMemo()
        );
    }
}
