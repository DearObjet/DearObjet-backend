package app.dearobjet.backend.domain.classes;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClassReservationRepository extends JpaRepository<ClassReservation, Long> {

    @Query("""
            SELECT r
            FROM ClassReservation r
            JOIN FETCH r.classes c
            JOIN FETCH c.shop s
            WHERE r.user.id = :userId
            ORDER BY r.reservationTime DESC
            """)
    List<ClassReservation> findByUserIdWithClassAndShop(@Param("userId") Long userId);
}
