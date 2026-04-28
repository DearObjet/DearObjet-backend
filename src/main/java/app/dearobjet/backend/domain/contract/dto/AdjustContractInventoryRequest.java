package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.enums.ContractProductStockMovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AdjustContractInventoryRequest {

    @NotNull(message = "재고 변경 타입은 필수입니다.")
    private ContractProductStockMovementType movementType;

    @NotNull(message = "재고 변경 수량은 필수입니다.")
    @Min(value = 1, message = "재고 변경 수량은 1 이상이어야 합니다.")
    private Integer quantity;

    @NotNull(message = "재고 변경 시각은 필수입니다.")
    private LocalDateTime occurredAt;

    private String memo;
}
