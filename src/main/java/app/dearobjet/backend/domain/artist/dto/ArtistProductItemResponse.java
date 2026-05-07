package app.dearobjet.backend.domain.artist.dto;

import app.dearobjet.backend.domain.shop.entity.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ArtistProductItemResponse {

    private final Long productId;
    private final String imageUrl;
    private final String productName;
    private final BigDecimal price;
    private final Integer stockQuantity;
    private final LocalDateTime registeredAt;
    private final Long version;

    public static ArtistProductItemResponse from(Product product) {
        return new ArtistProductItemResponse(
                product.getProductsId(),
                product.getProductUrl(),
                product.getProductName(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getCreatedAt(),
                product.getVersion()
        );
    }
}
