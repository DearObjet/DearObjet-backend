package app.dearobjet.backend.domain.artist.dto;

import app.dearobjet.backend.domain.shop.entity.Product;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ArtistShipmentAvailableProductItemResponse {

    private static final int DEFAULT_SHIPMENT_QUANTITY = 0;

    private final Long productId;
    private final String productImageUrl;
    private final String productName;
    private final BigDecimal sellingPrice;
    private final Integer shipmentQuantity;
    private final Integer totalQuantity;
    private final Long version;

    public static ArtistShipmentAvailableProductItemResponse from(Product product) {
        return new ArtistShipmentAvailableProductItemResponse(
                product.getProductsId(),
                product.getProductUrl(),
                product.getProductName(),
                product.getPrice(),
                DEFAULT_SHIPMENT_QUANTITY,
                product.getStockQuantity(),
                product.getVersion()
        );
    }
}
