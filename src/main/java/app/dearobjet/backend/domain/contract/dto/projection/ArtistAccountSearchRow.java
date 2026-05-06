package app.dearobjet.backend.domain.contract.dto.projection;

import app.dearobjet.backend.domain.user.enums.Specialty;

public interface ArtistAccountSearchRow {
    Long getArtistId();
    Long getUserId();
    String getName();
    String getArtistName();
    String getArtistImageUrl();
    Specialty getSpecialty();
    String getInstagramId();
    String getEmail();
}
