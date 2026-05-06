package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.dto.projection.ArtistSuggestionRow;
import app.dearobjet.backend.domain.user.enums.Specialty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ArtistSuggestionItemResponse {

    private final Long artistId;
    private final Long userId;
    private final String artistName;
    private final String artistImageUrl;
    private final Specialty specialty;
    private final String instagramId;

    public static ArtistSuggestionItemResponse from(ArtistSuggestionRow row) {
        return new ArtistSuggestionItemResponse(
                row.getArtistId(),
                row.getUserId(),
                row.getArtistName(),
                row.getArtistImageUrl(),
                row.getSpecialty(),
                row.getInstagramId()
        );
    }
}
