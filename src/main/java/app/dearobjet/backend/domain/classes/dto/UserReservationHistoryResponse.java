package app.dearobjet.backend.domain.classes.dto;

import app.dearobjet.backend.domain.classes.ClassReservation;
import app.dearobjet.backend.domain.classes.ReservationStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserReservationHistoryResponse {
    private Long reservationId;
    private String reservationShop;
    private LocalDateTime scheduleDateTime;
    private String className;
    private ReservationStatus reservationStatus;

    public static UserReservationHistoryResponse from(ClassReservation reservation) {
        return UserReservationHistoryResponse.builder()
                .reservationId(reservation.getReservationId())
                .reservationShop(reservation.getClasses().getShop().getShopName())
                .scheduleDateTime(reservation.getReservationTime())
                .className(reservation.getClasses().getClassName())
                .reservationStatus(reservation.getReservationStatus())
                .build();
    }
}
