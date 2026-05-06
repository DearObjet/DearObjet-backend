package app.dearobjet.backend.domain.contract.dto.projection;

import app.dearobjet.backend.domain.contract.enums.ContractStatus;

import java.time.LocalDate;

public interface ManagedArtistContractRow {
    Long getContractId();
    Long getArtistId();
    String getArtistName();
    LocalDate getContractStartDate();
    LocalDate getContractEndDate();
    ContractStatus getContractStatus();
}
