package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.dto.projection.ManagedShopContractRow;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ManagedShopContractListResponse {

    private final List<ManagedShopContractItemResponse> items;

    public static ManagedShopContractListResponse from(List<ManagedShopContractRow> rows, LocalDate today) {
        return new ManagedShopContractListResponse(
                rows.stream()
                        .map(row -> ManagedShopContractItemResponse.from(row, today))
                        .toList()
        );
    }
}
