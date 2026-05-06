package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.entity.ShopArtistContract;
import app.dearobjet.backend.domain.contract.enums.CommissionType;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ManagedArtistContractDetailResponse {

    private final Long contractId;
    private final Long shopId;
    private final String shopName;
    private final Long artistId;
    private final String artistName;
    private final LocalDate contractStartDate;
    private final LocalDate contractEndDate;
    private final ContractStatus contractStatus;
    private final CommissionType commissionType;
    private final BigDecimal commissionValue;
    private final String memo;

    public static ManagedArtistContractDetailResponse from(ShopArtistContract contract) {
        return new ManagedArtistContractDetailResponse(
                contract.getShopArtistContractsId(),
                contract.getShop().getShopId(),
                contract.getShop().getBusinessName(),
                contract.getArtist().getId(),
                contract.getArtist().getBusinessProfile().getBusinessName(),
                contract.getContractStartDate(),
                contract.getContractEndDate(),
                contract.getContractStatus(),
                contract.getCommissionType(),
                contract.getCommissionValue(),
                contract.getMemo() == null ? "" : contract.getMemo()
        );
    }
}
