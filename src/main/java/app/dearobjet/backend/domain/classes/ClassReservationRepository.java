package app.dearobjet.backend.domain.classes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ClassReservationRepository extends JpaRepository<ClassReservation, Long> {

    @EntityGraph(attributePaths = {"user", "classes"})
    Page<ClassReservation> findByClasses_Shop_User_Id(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "classes"})
    Page<ClassReservation> findByClasses_Shop_User_IdAndReservationStatus(
            Long userId,
            String reservationStatus,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"classes", "classes.shop", "classes.shop.user"})
    Page<ClassReservation> findByUser_Id(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"classes", "classes.shop", "classes.shop.user"})
    Page<ClassReservation> findByUser_IdAndReservationStatus(Long userId, String reservationStatus, Pageable pageable);

    @EntityGraph(attributePaths = {"classes", "classes.shop", "classes.shop.user"})
    Optional<ClassReservation> findByReservationIdAndUser_Id(Long reservationId, Long userId);

    @Query("""
            select coalesce(sum(r.guestCount), 0)
            from ClassReservation r
            where r.classes.classesId = :classId
              and r.reservationTime = :reservationTime
            """)
    int sumGuestCountByClasses_ClassesIdAndReservationTime(
            @Param("classId") Long classId,
            @Param("reservationTime") LocalDateTime reservationTime
    );
}
