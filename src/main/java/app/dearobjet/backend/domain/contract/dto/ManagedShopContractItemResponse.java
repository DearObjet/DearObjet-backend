package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.dto.projection.ManagedShopContractRow;
import app.dearobjet.backend.domain.contract.enums.ContractRequestType;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ManagedShopContractItemResponse {

    private final Long contractId;
    private final Long shopId;
    private final String shopName;
    private final LocalDate contractStartDate;
    private final LocalDate contractEndDate;
    private final String statusCode;
    private final String statusLabel;
    private final Boolean statusDisabled;
    private final List<ManagedShopContractActionResponse> actions;
    private final Boolean detailAvailable;

    public static ManagedShopContractItemResponse from(ManagedShopContractRow row, LocalDate today) {
        Display display = Display.from(row, today);

        return new ManagedShopContractItemResponse(
                row.getContractId(),
                row.getShopId(),
                row.getShopName(),
                row.getContractStartDate(),
                row.getContractEndDate(),
                display.statusCode(),
                display.statusLabel(),
                display.statusDisabled(),
                display.actions(),
                true
        );
    }

    private record Display(
            String statusCode,
            String statusLabel,
            Boolean statusDisabled,
            List<ManagedShopContractActionResponse> actions
    ) {
        private static Display from(ManagedShopContractRow row, LocalDate today) {
            if (row.getContractStatus() == ContractStatus.TERMINATED) {
                return new Display("EXPIRED", "계약만료", false, List.of());
            }
            if (row.getContractStatus() == ContractStatus.PENDING
                    && row.getContractRequestType() == ContractRequestType.RELEASE) {
                return new Display(
                        "EXTENSION_AVAILABLE",
                        "계약 연장",
                        true,
                        List.of(ManagedShopContractActionResponse.of("RELEASE_CANCEL", "해제취소", true))
                );
            }
            if (row.getContractStatus() == ContractStatus.PENDING) {
                return new Display(
                        "PENDING",
                        "계약 대기",
                        false,
                        List.of(ManagedShopContractActionResponse.of("WAITING_APPROVAL", "계약승인", false))
                );
            }
            if (row.getContractStatus() == ContractStatus.ENDED || isExpired(row, today)) {
                return new Display(
                        "EXTENSION_AVAILABLE",
                        "계약 연장",
                        false,
                        List.of(
                                ManagedShopContractActionResponse.of("EXTENSION_REQUEST", "계약연장", true),
                                ManagedShopContractActionResponse.of("RELEASE_REQUEST", "해제신청", true)
                        )
                );
            }
            if (row.getContractStatus() == ContractStatus.APPROVED) {
                return new Display(
                        "COMPLETED",
                        "계약 완료",
                        false,
                        List.of(ManagedShopContractActionResponse.of("CONTRACT_APPROVED", "계약승인", false))
                );
            }

            return new Display("UNAVAILABLE", "표시 불가", true, List.of());
        }

        private static boolean isExpired(ManagedShopContractRow row, LocalDate today) {
            LocalDate endDate = row.getContractEndDate();
            return row.getContractStatus() == ContractStatus.APPROVED
                    && endDate != null
                    && today != null
                    && today.isAfter(endDate);
        }
    }
}
