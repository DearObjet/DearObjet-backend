package app.dearobjet.backend.domain.contract.dto.projection;

import java.time.LocalDate;

public interface CompletedContractDocumentRow {
    Long getContractId();
    Long getArtistId();
    String getArtistName();
    String getArtistBusinessNumber();
    String getArtistContact();
    LocalDate getContractDate();
    LocalDate getContractStartDate();
    LocalDate getContractEndDate();
}
