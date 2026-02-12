package app.dearobjet.backend.domain.user.repository;

import app.dearobjet.backend.domain.artist.entity.Artist;
import app.dearobjet.backend.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository extends JpaRepository<Artist, Long> {

    Optional<Artist> findByUser(User user);

    Optional<Artist> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

}
