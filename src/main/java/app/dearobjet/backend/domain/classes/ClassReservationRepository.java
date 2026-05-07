package app.dearobjet.backend.domain.classes;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ClassReservationRepository extends JpaRepository<ClassReservation, Long> {

    @EntityGraph(attributePaths = {"user", "classes"})
    Page<ClassReservation> findByClasses_Shop_User_Id(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "classes"})
    Page<ClassReservation> findByClasses_Shop_User_IdAndReservationStatus(
            Long userId,
            ClassReservationStatus reservationStatus,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"classes", "classes.shop"})
    List<ClassReservation> findByUser_IdOrderByReservationTimeDesc(Long userId);

    boolean existsByUser_IdAndReservationStatus(Long userId, ClassReservationStatus reservationStatus);

    Optional<ClassReservation> findByReservationIdAndUser_Id(Long reservationId, Long userId);

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
