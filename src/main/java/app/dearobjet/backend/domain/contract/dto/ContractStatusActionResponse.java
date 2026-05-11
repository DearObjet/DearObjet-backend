package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.enums.ContractDocumentStatus;
import app.dearobjet.backend.domain.contract.enums.ContractRequestType;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContractStatusActionResponse {

    private final String code;
    private final String label;

    public static ContractStatusActionResponse from(
            ContractStatus status,
            ContractRequestType requestType,
            ContractDocumentStatus documentStatus,
            boolean terminable
    ) {
        if (status == ContractStatus.PENDING && requestType == ContractRequestType.RELEASE) {
            return new ContractStatusActionResponse("RELEASE_APPROVE", "해제 승인");
        }
        if (status == ContractStatus.PENDING
                && requestType == ContractRequestType.NONE
                && documentStatus == ContractDocumentStatus.SHOP_SENT) {
            return new ContractStatusActionResponse("WAITING_ARTIST_SUBMISSION", "작가 작성 대기");
        }
        if (status == ContractStatus.APPROVED && terminable) {
            return new ContractStatusActionResponse("TERMINATE_CONTRACT", "계약해지");
        }

        return switch (status) {
            case PENDING -> new ContractStatusActionResponse("CONTRACT_APPROVE", "계약 승인");
            case ENDED -> new ContractStatusActionResponse("TERMINATE_CONTRACT", "계약해지");
            case APPROVED, REJECTED, TERMINATED -> new ContractStatusActionResponse("NONE", "변경 불가");
        };
    }
}
