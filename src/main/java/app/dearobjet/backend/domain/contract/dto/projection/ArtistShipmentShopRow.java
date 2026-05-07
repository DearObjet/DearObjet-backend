package app.dearobjet.backend.domain.contract.dto.projection;

import app.dearobjet.backend.domain.user.enums.Specialty;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ArtistShipmentShopRow {
    Long getContractId();
    Long getShopId();
    String getShopName();
    Specialty getSpecialty();
    LocalDate getContractStartDate();
    LocalDate getContractEndDate();
    LocalDateTime getRecentStockedAt();
    Boolean getInboundConfirmed();
}
