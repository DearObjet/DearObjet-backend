package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.entity.ContractProduct;
import app.dearobjet.backend.domain.contract.entity.ContractProductStockMovement;
import app.dearobjet.backend.domain.contract.enums.ContractProductStockMovementType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AdjustContractInventoryResponse {

    private final Long contractProductId;
    private final ContractProductStockMovementType movementType;
    private final Integer quantityDelta;
    private final Integer stockQuantity;
    private final Integer soldQuantity;
    private final LocalDateTime recentStockedAt;
    private final LocalDateTime occurredAt;

    public static AdjustContractInventoryResponse from(
            ContractProduct contractProduct,
            ContractProductStockMovement movement
    ) {
        return new AdjustContractInventoryResponse(
                contractProduct.getContractProductsId(),
                movement.getMovementType(),
                movement.getQuantityDelta(),
                contractProduct.getStockQuantity(),
                contractProduct.getSoldQuantity(),
                contractProduct.getRecentStockedAt(),
                movement.getOccurredAt()
        );
    }
}
