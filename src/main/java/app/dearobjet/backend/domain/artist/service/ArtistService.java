package app.dearobjet.backend.domain.artist.service;

import app.dearobjet.backend.domain.artist.dto.ArtistListResponse;

public interface ArtistService {

    ArtistListResponse getArtists(Long seed, Long cursor, int size);
}
