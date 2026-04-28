package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.dto.projection.ContractInventoryRow;
import app.dearobjet.backend.domain.user.enums.Specialty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContractInventoryItemResponse {

    private final Long contractId;
    private final String artistImageUrl;
    private final String artistName;
    private final Specialty specialty;
    private final LocalDateTime recentStockedAt;
    private final Boolean inboundConfirmed;

    public static ContractInventoryItemResponse from(ContractInventoryRow row) {
        return new ContractInventoryItemResponse(
                row.getContractId(),
                row.getArtistImageUrl(),
                row.getArtistName(),
                row.getSpecialty(),
                row.getRecentStockedAt(),
                Boolean.TRUE.equals(row.getInboundConfirmed())
        );
    }
}
