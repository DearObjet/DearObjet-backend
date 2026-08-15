package app.dearobjet.backend.domain.classes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {

    List<ClassSession> findByClasses_IdAndStartDatetimeBetweenOrderByStartDatetimeAsc(
            Long classId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<ClassSession> findByShop_ShopIdAndStartDatetimeGreaterThanEqualOrderByStartDatetimeAsc(
            Long shopId,
            LocalDateTime startDatetime
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ClassSession> findBySessionId(Long sessionId);

    void deleteByClasses_Id(Long classId);
}
