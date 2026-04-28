package app.dearobjet.backend.domain.contract.entity;

import app.dearobjet.backend.domain.contract.enums.ContractProductListingStatus;
import app.dearobjet.backend.domain.contract.enums.ContractProductStockMovementType;
import app.dearobjet.backend.domain.shop.entity.Product;
import app.dearobjet.backend.global.common.entity.BaseTimeEntity;
import app.dearobjet.backend.global.exception.ErrorCode;
import app.dearobjet.backend.global.exception.InvalidInputException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 샵의 현재 재고 스냅샷
 */
@Entity
@Table(
        name = "contract_products",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_contract_products_contract_product",
                        columnNames = {"shop_artist_contracts_id", "products_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ContractProduct extends BaseTimeEntity {

    private static final int ZERO_STOCK_QUANTITY = 0;
    private static final int MIN_MOVEMENT_QUANTITY = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_products_id")
    private Long contractProductsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_artist_contracts_id", nullable = false)
    private ShopArtistContract shopArtistContract;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "listing_status", nullable = false, length = 20)
    private ContractProductListingStatus listingStatus = ContractProductListingStatus.ACTIVE;

    @Builder.Default
    @Column(name = "sold_quantity", nullable = false)
    private Integer soldQuantity = ZERO_STOCK_QUANTITY;

    @Builder.Default
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = ZERO_STOCK_QUANTITY;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "products_id", nullable = false)
    private Product product;

    @Column(name = "selling_price", precision = 19, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "margin_amount", precision = 19, scale = 2)
    private BigDecimal marginAmount; // 마진금액

    @Column(name = "unit_settlement_amount", precision = 19, scale = 2)
    private BigDecimal unitSettlementAmount; // 개당 정산금액

    @Column(name = "recent_stocked_at")
    private LocalDateTime recentStockedAt; // 최근입고일

    @Version
    @Column(name = "version")
    private Long version;

    /**
     * 정산 관련 금액은 재고 이력과 분리해서 관리한다.
     */
    public void updateInventoryManagement(
            BigDecimal marginAmount,
            BigDecimal unitSettlementAmount
    ) {
        this.marginAmount = marginAmount;
        this.unitSettlementAmount = unitSettlementAmount;
    }

    public ContractProductStockMovement applyInbound(
            int quantity,
            LocalDateTime occurredAt,
            Long createdByUserId,
            String memo
    ) {
        return applyIncreaseMovement(
                ContractProductStockMovementType.INBOUND,
                quantity,
                occurredAt,
                createdByUserId,
                memo,
                true
        );
    }

    public ContractProductStockMovement applyAdjustmentIncrease(
            int quantity,
            LocalDateTime occurredAt,
            Long createdByUserId,
            String memo
    ) {
        return applyIncreaseMovement(
                ContractProductStockMovementType.ADJUSTMENT_INCREASE,
                quantity,
                occurredAt,
                createdByUserId,
                memo,
                false
        );
    }

    public ContractProductStockMovement applyAdjustmentDecrease(
            int quantity,
            LocalDateTime occurredAt,
            Long createdByUserId,
            String memo
    ) {
        return applyDecreaseMovement(
                ContractProductStockMovementType.ADJUSTMENT_DECREASE,
                quantity,
                occurredAt,
                createdByUserId,
                memo,
                false
        );
    }

    public ContractProductStockMovement applySaleDecrease(
            int quantity,
            LocalDateTime occurredAt,
            Long createdByUserId,
            String memo
    ) {
        ContractProductStockMovement movement = applyDecreaseMovement(
                ContractProductStockMovementType.SALE_DECREASE,
                quantity,
                occurredAt,
                createdByUserId,
                memo,
                false
        );
        this.soldQuantity += quantity;
        return movement;
    }

    public ContractProductStockMovement applyReturnIncrease(
            int quantity,
            LocalDateTime occurredAt,
            Long createdByUserId,
            String memo
    ) {
        return applyIncreaseMovement(
                ContractProductStockMovementType.RETURN_INCREASE,
                quantity,
                occurredAt,
                createdByUserId,
                memo,
                false
        );
    }

    public ContractProductStockMovement applyCancelRestore(
            int quantity,
            LocalDateTime occurredAt,
            Long createdByUserId,
            String memo
    ) {
        if (this.soldQuantity < quantity) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "복구할 판매 수량이 현재 판매 수량보다 많습니다.");
        }

        ContractProductStockMovement movement = applyIncreaseMovement(
                ContractProductStockMovementType.CANCEL_RESTORE,
                quantity,
                occurredAt,
                createdByUserId,
                memo,
                false
        );
        this.soldQuantity -= quantity;
        return movement;
    }

    private ContractProductStockMovement applyIncreaseMovement(
            ContractProductStockMovementType movementType,
            int quantity,
            LocalDateTime occurredAt,
            Long createdByUserId,
            String memo,
            boolean updateRecentStockedAt
    ) {
        validateMovementMetadata(quantity, occurredAt, createdByUserId);
        this.stockQuantity += quantity;
        if (updateRecentStockedAt) {
            this.recentStockedAt = occurredAt;
        }
        return createMovement(movementType, quantity, occurredAt, createdByUserId, memo);
    }

    private ContractProductStockMovement applyDecreaseMovement(
            ContractProductStockMovementType movementType,
            int quantity,
            LocalDateTime occurredAt,
            Long createdByUserId,
            String memo,
            boolean updateRecentStockedAt
    ) {
        validateMovementMetadata(quantity, occurredAt, createdByUserId);
        if (this.stockQuantity < quantity) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "현재 재고보다 많은 수량을 차감할 수 없습니다.");
        }

        this.stockQuantity -= quantity;
        if (updateRecentStockedAt) {
            this.recentStockedAt = occurredAt;
        }
        return createMovement(movementType, -quantity, occurredAt, createdByUserId, memo);
    }

    /**
     * 현재 재고 스냅샷과 이력 원본이 항상 같은 결과를 보도록 한 규칙으로 movement를 만든다.
     */
    private ContractProductStockMovement createMovement(
            ContractProductStockMovementType movementType,
            int quantityDelta,
            LocalDateTime occurredAt,
            Long createdByUserId,
            String memo
    ) {
        return ContractProductStockMovement.create(
                this,
                movementType,
                quantityDelta,
                this.stockQuantity,
                occurredAt,
                memo,
                createdByUserId
        );
    }

    private void validateMovementMetadata(
            int quantity,
            LocalDateTime occurredAt,
            Long createdByUserId
    ) {
        if (quantity < MIN_MOVEMENT_QUANTITY) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "재고 변경 수량은 1 이상이어야 합니다.");
        }
        if (occurredAt == null) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "재고 변경 시각은 필수입니다.");
        }
        if (createdByUserId == null) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "재고 변경 사용자 ID는 필수입니다.");
        }
    }
}
