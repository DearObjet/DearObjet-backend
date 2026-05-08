package app.dearobjet.backend.domain.artist.dto;

import app.dearobjet.backend.domain.contract.dto.projection.ArtistShipmentProductRow;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ArtistShipmentProductItemResponse {

    private final Long contractProductId;
    private final String productImageUrl;
    private final String productName;
    private final Long totalShipmentQuantity;
    private final BigDecimal sellingPrice;

    public static ArtistShipmentProductItemResponse from(ArtistShipmentProductRow row) {
        return new ArtistShipmentProductItemResponse(
                row.getContractProductId(),
                row.getProductImageUrl(),
                row.getProductName(),
                row.getTotalShipmentQuantity(),
                row.getSellingPrice()
        );
    }
}
