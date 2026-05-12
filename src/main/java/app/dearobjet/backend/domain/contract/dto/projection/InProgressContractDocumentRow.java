package app.dearobjet.backend.domain.contract.dto.projection;

import app.dearobjet.backend.domain.contract.enums.ContractDocumentStatus;

import java.time.LocalDate;

public interface InProgressContractDocumentRow {
    Long getContractId();
    Long getCounterpartyId();
    String getCounterpartyName();
    LocalDate getContractDate();
    LocalDate getContractStartDate();
    LocalDate getContractEndDate();
    ContractDocumentStatus getContractDocumentStatus();
}
