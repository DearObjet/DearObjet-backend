package app.dearobjet.backend.domain.contract.dto.projection;

import app.dearobjet.backend.domain.user.enums.Specialty;

public interface ArtistSuggestionRow {
    Long getArtistId();
    Long getUserId();
    String getArtistName();
    String getArtistImageUrl();
    Specialty getSpecialty();
    String getInstagramId();
}
