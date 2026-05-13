package app.dearobjet.backend.domain.contract.dto.projection;

import app.dearobjet.backend.domain.user.enums.Specialty;

public interface ShopSuggestionRow {
    Long getShopId();
    Long getUserId();
    String getShopName();
    String getShopImageUrl();
    Specialty getSpecialty();
    String getInstagramId();
}
