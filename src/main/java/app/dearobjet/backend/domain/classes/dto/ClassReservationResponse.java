package app.dearobjet.backend.domain.classes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ClassReservationResponse {
    private final String status;
    private final String reservationName;
    private final String phoneNumber;
    private final Long reservationId;
    private final LocalDateTime reservationTime;
    private final String className;
    private final Integer guestCount;
    private final String memo;
}
