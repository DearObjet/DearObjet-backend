package app.dearobjet.backend.domain.classes;
import app.dearobjet.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(
        name = "reservation",
        indexes = {
                @Index(name = "idx_reservation_user_id", columnList = "user_id"),
                @Index(name = "idx_reservation_time", columnList = "reservation_time")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ClassReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "guest_count", nullable = false)
    private Integer guestCount;

    @Column(name = "reservation_time", nullable = false)
    private java.time.LocalDateTime reservationTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status", nullable = false, length = 20)
    private ReservationStatus reservationStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classes_id", nullable = false)
    private Classes classes;
}
