package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.dto.projection.ManagedArtistContractRow;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ManagedArtistContractItemResponse {

    private final Long contractId;
    private final Long artistId;
    private final String artistName;
    private final LocalDate contractStartDate;
    private final LocalDate contractEndDate;
    private final ContractStatus contractStatus;
    private final String contractStatusLabel;
    private final ContractStatusActionResponse nextAction;
    private final Boolean detailAvailable;

    public static ManagedArtistContractItemResponse from(ManagedArtistContractRow row) {
        ContractStatus status = row.getContractStatus();

        return new ManagedArtistContractItemResponse(
                row.getContractId(),
                row.getArtistId(),
                row.getArtistName(),
                row.getContractStartDate(),
                row.getContractEndDate(),
                status,
                statusLabel(status),
                ContractStatusActionResponse.from(status),
                true
        );
    }

    private static String statusLabel(ContractStatus status) {
        return switch (status) {
            case PENDING -> "계약 대기";
            case APPROVED -> "계약중";
            case ENDED -> "계약 만료";
            case REJECTED -> "계약 거절";
        };
    }
}
