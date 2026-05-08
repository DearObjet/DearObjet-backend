package app.dearobjet.backend.domain.contract.dto.projection;

import app.dearobjet.backend.domain.contract.enums.CommissionType;

import java.math.BigDecimal;

public interface ArtistShipmentProductRow {
    Long getContractId();
    Long getContractProductId();
    String getProductImageUrl();
    String getProductName();
    Long getTotalShipmentQuantity();
    BigDecimal getSellingPrice();
    CommissionType getCommissionType();
    BigDecimal getCommissionValue();
    BigDecimal getUnitSettlementAmount();
}
