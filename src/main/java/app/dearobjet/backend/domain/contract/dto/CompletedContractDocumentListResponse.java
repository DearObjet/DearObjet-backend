package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.dto.projection.CompletedContractDocumentRow;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CompletedContractDocumentListResponse {

    private final List<CompletedContractDocumentItemResponse> items;

    public static CompletedContractDocumentListResponse from(List<CompletedContractDocumentRow> rows) {
        return new CompletedContractDocumentListResponse(
                rows.stream()
                        .map(CompletedContractDocumentItemResponse::from)
                        .toList()
        );
    }
}
