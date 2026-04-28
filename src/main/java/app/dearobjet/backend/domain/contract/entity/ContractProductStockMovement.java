package app.dearobjet.backend.domain.contract.entity;

import app.dearobjet.backend.domain.contract.enums.ContractProductStockMovementType;
import app.dearobjet.backend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 샵의 재고 입고/조정 이력
 */
@Entity
@Table(
        name = "contract_product_stock_movements",
        indexes = {
                @Index(
                        name = "idx_contract_product_stock_movements_contract_product",
                        columnList = "contract_products_id"
                ),
                @Index(
                        name = "idx_contract_product_stock_movements_occurred_at",
                        columnList = "occurred_at"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class ContractProductStockMovement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_product_stock_movement_id")
    private Long contractProductStockMovementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_products_id", nullable = false)
    private ContractProduct contractProduct;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 30)
    private ContractProductStockMovementType movementType;

    @Column(name = "quantity_delta", nullable = false)
    private Integer quantityDelta;

    @Column(name = "result_stock_quantity", nullable = false)
    private Integer resultStockQuantity;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    public static ContractProductStockMovement create(
            ContractProduct contractProduct,
            ContractProductStockMovementType movementType,
            Integer quantityDelta,
            Integer resultStockQuantity,
            LocalDateTime occurredAt,
            String memo,
            Long createdByUserId
    ) {
        return ContractProductStockMovement.builder()
                .contractProduct(contractProduct)
                .movementType(movementType)
                .quantityDelta(quantityDelta)
                .resultStockQuantity(resultStockQuantity)
                .occurredAt(occurredAt)
                .memo(memo)
                .createdByUserId(createdByUserId)
                .build();
    }
}
