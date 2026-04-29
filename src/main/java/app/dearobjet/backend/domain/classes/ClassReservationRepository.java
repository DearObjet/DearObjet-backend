package app.dearobjet.backend.domain.classes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClassReservationRepository extends JpaRepository<ClassReservation, Long> {

    @EntityGraph(attributePaths = {"user", "classes"})
    List<ClassReservation> findByClasses_Shop_User_IdAndReservationTimeGreaterThanEqualAndReservationTimeLessThanOrderByReservationTimeAscReservationIdAsc(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<ClassReservation> findByClasses_ClassesIdAndReservationTimeBetween(
            Long classesId,
            LocalDateTime start,
            LocalDateTime end
    );
    @Query("""
            select coalesce(sum(r.guestCount), 0)
            from ClassReservation r
            where r.classSession.sessionId = :sessionId
            and r.reservationStatus <> app.dearobjet.backend.domain.classes.ClassReservationStatus.CANCELED
            """)
    int sumGuestCountBySessionId(@Param("sessionId") Long sessionId);

    @Query("""
            select count(r)
            from ClassReservation r
            where r.classSession.sessionId = :sessionId
            and r.reservationStatus <> app.dearobjet.backend.domain.classes.ClassReservationStatus.CANCELED
            """)
    long countActiveReservationsBySessionId(@Param("sessionId") Long sessionId);
}
