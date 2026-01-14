package app.dearobjet.backend.domain.classes;
import app.dearobjet.backend.domain.shop.entity.Shop;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "reservation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class  ClassReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "guest_count")
    private Integer guestCount;

    @Column(name = "reservation_time")
    private java.time.LocalDateTime reservationTime;

    @Column(name = "reservation_status")
    private String reservationStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classes_id", nullable = false)
    private Classes classes;
}