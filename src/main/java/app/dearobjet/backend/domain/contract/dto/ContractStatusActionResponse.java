package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContractStatusActionResponse {

    private final String code;
    private final String label;

    public static ContractStatusActionResponse from(ContractStatus status) {
        return switch (status) {
            case PENDING -> new ContractStatusActionResponse("CONTRACT_APPROVE", "계약 승인");
            case APPROVED -> new ContractStatusActionResponse("RELEASE_APPROVE", "해제 승인");
            case ENDED -> new ContractStatusActionResponse("CONTRACTING", "계약중");
            case REJECTED -> new ContractStatusActionResponse("NONE", "변경 불가");
        };
    }
}
