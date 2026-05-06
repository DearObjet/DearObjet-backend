package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.entity.ShopArtistContract;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContractTerminationResponse {

    private final Long contractId;
    private final ContractStatus contractStatus;

    public static ContractTerminationResponse from(ShopArtistContract contract) {
        return new ContractTerminationResponse(
                contract.getShopArtistContractsId(),
                contract.getContractStatus()
        );
    }
}
