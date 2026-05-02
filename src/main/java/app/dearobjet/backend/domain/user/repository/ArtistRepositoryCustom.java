package app.dearobjet.backend.domain.user.repository;

import app.dearobjet.backend.domain.artist.entity.Artist;
import java.util.List;

public interface ArtistRepositoryCustom {

    List<Artist> findRandomArtists(long seed, Long cursorArtistId, int limit);
}
