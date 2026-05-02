package app.dearobjet.backend.domain.artist.dto;

import app.dearobjet.backend.domain.artist.entity.Artist;

public record ArtistResponse(
        Long artistId,
        String profileUrl,
        String name
) {
    public static ArtistResponse from(Artist artist) {
        return new ArtistResponse(
                artist.getId(),
                artist.getUser().getProfileUrl(),
                artist.getUser().getName()
        );
    }
}
