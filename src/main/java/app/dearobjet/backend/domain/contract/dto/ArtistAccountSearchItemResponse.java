package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.dto.projection.ArtistAccountSearchRow;
import app.dearobjet.backend.domain.user.enums.Specialty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ArtistAccountSearchItemResponse {

    private final Long artistId;
    private final Long userId;
    private final String userName;
    private final String artistName;
    private final String artistImageUrl;
    private final Specialty specialty;
    private final String instagramId;
    private final String email;

    public static ArtistAccountSearchItemResponse from(ArtistAccountSearchRow row) {
        return new ArtistAccountSearchItemResponse(
                row.getArtistId(),
                row.getUserId(),
                row.getName(),
                row.getArtistName(),
                row.getArtistImageUrl(),
                row.getSpecialty(),
                row.getInstagramId(),
                row.getEmail()
        );
    }
}
