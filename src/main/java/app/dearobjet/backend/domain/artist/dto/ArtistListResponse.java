package app.dearobjet.backend.domain.artist.dto;

import java.util.List;

public record ArtistListResponse(
        List<ArtistResponse> artists,
        Long seed,
        Long nextCursor,
        boolean hasNext
) {
}
