package app.dearobjet.backend.domain.contract.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteCompletedContractDocumentsResponse {

    private final int deletedCount;
    private final List<Long> deletedContractIds;

    public static DeleteCompletedContractDocumentsResponse from(List<Long> deletedContractIds) {
        return new DeleteCompletedContractDocumentsResponse(
                deletedContractIds.size(),
                deletedContractIds
        );
    }
}
