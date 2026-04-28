package app.dearobjet.backend.domain.contract.dto.projection;

import app.dearobjet.backend.domain.contract.enums.CommissionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ContractInventoryProductRow {
    Long getContractProductId();
    Long getTotalQuantity();
    String getProductImageUrl();
    String getProductName();
    BigDecimal getSellingPrice();
    Integer getStockQuantity();
    CommissionType getCommissionType();
    BigDecimal getCommissionValue();
    BigDecimal getMarginAmount();
    BigDecimal getUnitSettlementAmount();
    String getArtistName();
    LocalDateTime getRecentStockedAt();
}
