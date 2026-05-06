package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.dto.projection.ArtistAccountSearchRow;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ArtistAccountSearchResponse {

    private final List<ArtistAccountSearchItemResponse> items;

    public static ArtistAccountSearchResponse from(List<ArtistAccountSearchRow> rows) {
        return new ArtistAccountSearchResponse(
                rows.stream()
                        .map(ArtistAccountSearchItemResponse::from)
                        .toList()
        );
    }
}
