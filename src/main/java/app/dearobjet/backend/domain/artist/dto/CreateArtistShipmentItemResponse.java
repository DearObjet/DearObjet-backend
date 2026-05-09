package app.dearobjet.backend.domain.artist.dto;

import app.dearobjet.backend.domain.contract.entity.ContractProduct;
import app.dearobjet.backend.domain.contract.enums.CommissionType;
import app.dearobjet.backend.domain.shop.entity.Product;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateArtistShipmentItemResponse {

    private final Long productId;
    private final Long contractProductId;
    private final String productImageUrl;
    private final String productName;
    private final BigDecimal sellingPrice;
    private final Integer shipmentQuantity;
    private final Integer remainingQuantity;
    private final CommissionType commissionType;
    private final BigDecimal commissionValue;
    private final BigDecimal unitSettlementAmount;

    public static CreateArtistShipmentItemResponse from(
            Product product,
            ContractProduct contractProduct,
            int shipmentQuantity
    ) {
        return new CreateArtistShipmentItemResponse(
                product.getProductsId(),
                contractProduct.getContractProductsId(),
                product.getProductUrl(),
                product.getProductName(),
                contractProduct.getSellingPrice(),
                shipmentQuantity,
                product.getStockQuantity(),
                contractProduct.getShopArtistContract().getCommissionType(),
                contractProduct.getShopArtistContract().getCommissionValue(),
                contractProduct.getUnitSettlementAmount()
        );
    }
}
