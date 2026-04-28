package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.dto.projection.ContractInventoryRow;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContractInventoryListResponse {

    private final List<ContractInventoryItemResponse> items;

    public static ContractInventoryListResponse from(List<ContractInventoryRow> rows) {
        return new ContractInventoryListResponse(
                rows.stream()
                        .map(ContractInventoryItemResponse::from)
                        .toList()
        );
    }
}
