package app.dearobjet.backend.domain.contract.dto.projection;

import app.dearobjet.backend.domain.contract.enums.ContractDocumentStatus;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import app.dearobjet.backend.domain.contract.enums.ContractRequestType;

import java.time.LocalDate;

public interface ManagedArtistContractRow {
    Long getContractId();
    Long getArtistId();
    String getArtistName();
    LocalDate getContractStartDate();
    LocalDate getContractEndDate();
    ContractStatus getContractStatus();
    ContractRequestType getContractRequestType();
    ContractDocumentStatus getContractDocumentStatus();
}
