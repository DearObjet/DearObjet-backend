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

    @Version
    @Column(name = "version")
    private Long version;

    public void updateStockQuantity(int stockQuantity, Long expectedVersion) {
        validateStockQuantity(stockQuantity);
        validateVersion(expectedVersion);

        this.stockQuantity = stockQuantity;
    }

    public void deactivate() {
        this.status = ProductStatus.INACTIVE;
    }

    private void validateStockQuantity(int stockQuantity) {
        if (stockQuantity < MIN_STOCK_QUANTITY) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "재고 수량은 0 이상이어야 합니다.");
        }
    }

    private void validateVersion(Long expectedVersion) {
        if (!Objects.equals(this.version, expectedVersion)) {
            throw new BusinessException(ErrorCode.PRODUCT_STOCK_CONFLICT);
        }
    }
}
