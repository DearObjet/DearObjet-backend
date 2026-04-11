package app.dearobjet.backend.domain.classes;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassReservationRepository extends JpaRepository<ClassReservation, Long> {

    @EntityGraph(attributePaths = {"user", "classes"})
    Page<ClassReservation> findByClasses_Shop_User_Id(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "classes"})
    Page<ClassReservation> findByClasses_Shop_User_IdAndReservationStatus(
            Long userId,
            String reservationStatus,
            Pageable pageable
    );

    List<ClassReservation> findByClasses_ClassesIdAndReservationTimeBetween(
            Long classesId,
            LocalDateTime start,
            LocalDateTime end
    );
}
