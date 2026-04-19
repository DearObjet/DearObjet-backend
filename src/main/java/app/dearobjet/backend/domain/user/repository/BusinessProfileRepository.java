package app.dearobjet.backend.domain.user.repository;

import app.dearobjet.backend.domain.user.entity.BusinessProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {

    Optional<BusinessProfile> findByUserId(Long userId);

    boolean existsByBusinessNumber(String businessNumber);
}
