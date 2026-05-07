package app.dearobjet.backend.domain.shop.entity;

import app.dearobjet.backend.domain.artist.entity.Artist;
import app.dearobjet.backend.domain.shop.enums.ProductStatus;
import app.dearobjet.backend.global.common.entity.BaseTimeEntity;
import app.dearobjet.backend.global.exception.BusinessException;
import app.dearobjet.backend.global.exception.ErrorCode;
import app.dearobjet.backend.global.exception.InvalidInputException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 작가의 품목 관리 엔티티
 */
@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Product extends BaseTimeEntity {

    private static final int MIN_STOCK_QUANTITY = 0;
    private static final BigDecimal MIN_PRICE = BigDecimal.ZERO;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "products_id")
    private Long productsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id")
    private Artist artist;

    @Column(name = "product_name")
    private String productName;

    @Column(precision = 19, scale = 2)
    private BigDecimal price;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(name = "product_url")
    private String productUrl;

    @Builder.Default
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = MIN_STOCK_QUANTITY;

    @Builder.Default
    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo = "";

    @Version
    @Column(name = "version")
    private Long version;

    public static Product create(
            Artist artist,
            String productName,
            BigDecimal price,
            int stockQuantity,
            String productUrl
    ) {
        Product product = Product.builder()
                .artist(artist)
                .status(ProductStatus.ACTIVE)
                .build();
        product.updateProductInfo(productName, price, stockQuantity, productUrl);
        return product;
    }

    public void updateProductInfo(
            String productName,
            BigDecimal price,
            int stockQuantity,
            String productUrl
    ) {
        validateProductName(productName);
        validatePrice(price);
        validateStockQuantity(stockQuantity);
        validateProductUrl(productUrl);

        this.productName = productName.trim();
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.productUrl = productUrl;
    }

    public void updateProductInfoKeepingImage(
            String productName,
            BigDecimal price,
            int stockQuantity
    ) {
        updateProductInfo(productName, price, stockQuantity, this.productUrl);
    }

    public void updateStockQuantity(int stockQuantity, Long expectedVersion) {
        validateStockQuantity(stockQuantity);
        validateVersion(expectedVersion);

        this.stockQuantity = stockQuantity;
    }

    public void updateMemo(String memo) {
        if (memo == null || memo.isBlank()) {
            this.memo = "";
            return;
        }
        this.memo = memo.trim();
    }

    public void deactivate() {
        this.status = ProductStatus.INACTIVE;
    }

    private void validateStockQuantity(int stockQuantity) {
        if (stockQuantity < MIN_STOCK_QUANTITY) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "재고 수량은 0 이상이어야 합니다.");
        }
    }

    private void validateProductName(String productName) {
        if (productName == null || productName.isBlank()) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "상품명은 필수입니다.");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(MIN_PRICE) < 0) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "판매가는 0 이상이어야 합니다.");
        }
    }

    private void validateProductUrl(String productUrl) {
        if (productUrl == null || productUrl.isBlank()) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "상품 이미지는 필수입니다.");
        }
    }

    private void validateVersion(Long expectedVersion) {
        if (!Objects.equals(this.version, expectedVersion)) {
            throw new BusinessException(ErrorCode.PRODUCT_STOCK_CONFLICT);
        }
    }
}
