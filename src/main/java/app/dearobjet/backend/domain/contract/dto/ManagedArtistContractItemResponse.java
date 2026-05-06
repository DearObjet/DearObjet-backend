package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.enums.ContractRequestType;
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

    public static ManagedArtistContractItemResponse from(ManagedArtistContractRow row, LocalDate today) {
        ContractStatus displayStatus = displayStatus(row, today);
        boolean terminable = isTerminable(row, today);

        return new ManagedArtistContractItemResponse(
                row.getContractId(),
                row.getArtistId(),
                row.getArtistName(),
                row.getContractStartDate(),
                row.getContractEndDate(),
                displayStatus,
                statusLabel(displayStatus),
                ContractStatusActionResponse.from(
                        row.getContractStatus(),
                        row.getContractRequestType(),
                        terminable
                ),
                true
        );
    }

    private static ContractStatus displayStatus(ManagedArtistContractRow row, LocalDate today) {
        if (row.getContractStatus() == ContractStatus.APPROVED && isTerminable(row, today)) {
            return ContractStatus.ENDED;
        }

        return row.getContractStatus();
    }

    private static boolean isTerminable(ManagedArtistContractRow row, LocalDate today) {
        LocalDate endDate = row.getContractEndDate();
        if (endDate == null || today == null) {
            return false;
        }

        // 계약 종료일 이후 작가 응답이 없는 경우, 14일째부터 소품샵 해지를 노출한다.
        return !today.isBefore(endDate.plusDays(14));
    }

    private static String statusLabel(ContractStatus status) {
        return switch (status) {
            case PENDING -> "계약 대기";
            case APPROVED -> "계약중";
            case ENDED -> "계약 만료";
            case REJECTED -> "계약 거절";
            case TERMINATED -> "계약 해지";
        };
    }
}
