package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.entity.ShopArtistContract;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContractInboundConfirmResponse {

    private final Long contractId;
    private final Boolean inboundConfirmed;
    private final LocalDateTime confirmedAt;

    public static ContractInboundConfirmResponse from(ShopArtistContract contract) {
        return new ContractInboundConfirmResponse(
                contract.getShopArtistContractsId(),
                Boolean.TRUE.equals(contract.getRecentInboundConfirmed()),
                contract.getRecentInboundConfirmedAt()
        );
    }
}
