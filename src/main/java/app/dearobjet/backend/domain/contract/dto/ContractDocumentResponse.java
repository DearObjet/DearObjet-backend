package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.entity.ShopArtistContract;
import app.dearobjet.backend.domain.contract.enums.ContractDocumentStatus;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContractDocumentResponse {

    private final Long contractId;
    private final ContractStatus contractStatus;
    private final ContractDocumentStatus contractDocumentStatus;
    private final String shopBusinessName;
    private final String shopOwnerName;
    private final String shopBusinessNumber;
    private final String shopAddress;
    private final String shopContact;
    private final LocalDate contractStartDate;
    private final LocalDate contractEndDate;
    private final BigDecimal commissionRate;
    private final Integer settlementDay;
    private final Integer paymentDay;
    private final LocalDate contractDate;
    private final String shopSignatureBusinessName;
    private final String shopSignatureOwnerName;
    private final String artistName;
    private final String artistBusinessNumber;
    private final String artistAddress;
    private final String artistContact;
    private final String artistBankName;
    private final String artistAccountHolder;
    private final String artistAccountNumber;
    private final String artistSignatureName;

    public static ContractDocumentResponse from(ShopArtistContract contract) {
        return new ContractDocumentResponse(
                contract.getShopArtistContractsId(),
                contract.getContractStatus(),
                contract.getContractDocumentStatus(),
                valueOrEmpty(contract.getShopContractBusinessName()),
                valueOrEmpty(contract.getShopContractOwnerName()),
                valueOrEmpty(contract.getShopContractBusinessNumber()),
                valueOrEmpty(contract.getShopContractAddress()),
                valueOrEmpty(contract.getShopContractContact()),
                contract.getContractStartDate(),
                contract.getContractEndDate(),
                contract.getCommissionValue(),
                contract.getSettlementDay(),
                contract.getPaymentDay(),
                contract.getContractDate(),
                valueOrEmpty(contract.getShopSignatureBusinessName()),
                valueOrEmpty(contract.getShopSignatureOwnerName()),
                valueOrEmpty(contract.getArtistContractName()),
                valueOrEmpty(contract.getArtistContractBusinessNumber()),
                valueOrEmpty(contract.getArtistContractAddress()),
                valueOrEmpty(contract.getArtistContractContact()),
                valueOrEmpty(contract.getArtistBankName()),
                valueOrEmpty(contract.getArtistAccountHolder()),
                valueOrEmpty(contract.getArtistAccountNumber()),
                valueOrEmpty(contract.getArtistSignatureName())
        );
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
