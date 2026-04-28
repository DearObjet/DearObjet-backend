package app.dearobjet.backend.domain.contract.dto.projection;

import app.dearobjet.backend.domain.user.enums.Specialty;

import java.time.LocalDateTime;

public interface ContractInventoryRow {
    Long getContractId();
    String getArtistImageUrl();
    String getArtistName();
    Specialty getSpecialty();
    LocalDateTime getRecentStockedAt();
    Boolean getInboundConfirmed();
}
