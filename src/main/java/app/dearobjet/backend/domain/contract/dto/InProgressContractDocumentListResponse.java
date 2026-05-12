package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.dto.projection.InProgressContractDocumentRow;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.function.Function;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InProgressContractDocumentListResponse {

    private final List<InProgressContractDocumentItemResponse> items;

    public static InProgressContractDocumentListResponse forShop(List<InProgressContractDocumentRow> rows) {
        return from(rows, InProgressContractDocumentItemResponse::forShop);
    }

    public static InProgressContractDocumentListResponse forArtist(List<InProgressContractDocumentRow> rows) {
        return from(rows, InProgressContractDocumentItemResponse::forArtist);
    }

    private static InProgressContractDocumentListResponse from(
            List<InProgressContractDocumentRow> rows,
            Function<InProgressContractDocumentRow, InProgressContractDocumentItemResponse> mapper
    ) {
        return new InProgressContractDocumentListResponse(
                rows.stream()
                        .map(mapper)
                        .toList()
        );
    }
}
