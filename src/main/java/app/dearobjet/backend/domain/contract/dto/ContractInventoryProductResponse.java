package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.dto.projection.ContractInventoryProductRow;
import app.dearobjet.backend.domain.contract.enums.CommissionType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContractInventoryProductResponse {

    private final Long contractProductId;
    private final Long totalQuantity;
    private final String productImageUrl;
    private final String productName;
    private final BigDecimal sellingPrice;
    private final Integer stockQuantity;
    private final CommissionType commissionType;
    private final BigDecimal commissionValue;
    private final BigDecimal marginAmount;
    private final BigDecimal unitSettlementAmount;
    private final String artistName;
    private final LocalDateTime recentStockedAt;

    public static ContractInventoryProductResponse from(ContractInventoryProductRow row) {
        return new ContractInventoryProductResponse(
                row.getContractProductId(),
                row.getTotalQuantity(),
                row.getProductImageUrl(),
                row.getProductName(),
                row.getSellingPrice(),
                row.getStockQuantity(),
                row.getCommissionType(),
                row.getCommissionValue(),
                row.getMarginAmount(),
                row.getUnitSettlementAmount(),
                row.getArtistName(),
                row.getRecentStockedAt()
        );
    }
}
