package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.dto.projection.CompletedContractDocumentRow;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CompletedContractDocumentItemResponse {

    private static final String CONTRACT_TITLE = "입점 계약서";

    private final Long contractId;
    private final String title;
    private final LocalDate contractDate;
    private final Long artistId;
    private final String artistName;
    private final String artistBusinessNumber;
    private final String artistContact;
    private final LocalDate contractStartDate;
    private final LocalDate contractEndDate;

    public static CompletedContractDocumentItemResponse from(CompletedContractDocumentRow row) {
        return new CompletedContractDocumentItemResponse(
                row.getContractId(),
                CONTRACT_TITLE,
                row.getContractDate(),
                row.getArtistId(),
                valueOrEmpty(row.getArtistName()),
                valueOrEmpty(row.getArtistBusinessNumber()),
                valueOrEmpty(row.getArtistContact()),
                row.getContractStartDate(),
                row.getContractEndDate()
        );
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
