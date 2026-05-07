package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.entity.ShopArtistContract;
import app.dearobjet.backend.domain.contract.enums.ContractRequestType;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ManagedShopContractActionResultResponse {

    private final Long contractId;
    private final ContractStatus contractStatus;
    private final ContractRequestType contractRequestType;

    public static ManagedShopContractActionResultResponse from(ShopArtistContract contract) {
        return new ManagedShopContractActionResultResponse(
                contract.getShopArtistContractsId(),
                contract.getContractStatus(),
                contract.getContractRequestType()
        );
    }
}
