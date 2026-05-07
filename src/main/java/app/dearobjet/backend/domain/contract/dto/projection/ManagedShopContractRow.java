package app.dearobjet.backend.domain.contract.dto.projection;

import app.dearobjet.backend.domain.contract.enums.ContractRequestType;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ManagedShopContractRow {
    Long getContractId();
    Long getShopId();
    String getShopName();
    LocalDate getContractStartDate();
    LocalDate getContractEndDate();
    ContractStatus getContractStatus();
    ContractRequestType getContractRequestType();
    LocalDateTime getTerminatedAt();
}
