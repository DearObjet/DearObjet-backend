package app.dearobjet.backend.domain.contract.dto.projection;

import java.math.BigDecimal;

public interface ArtistShipmentProductRow {
    Long getContractProductId();
    String getProductImageUrl();
    String getProductName();
    Long getTotalShipmentQuantity();
    BigDecimal getSellingPrice();
}
