package app.dearobjet.backend.domain.classes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ClassReservationListResponse {
    private final long reservationCount;
    private final List<Item> reservations;

    @Getter
    @AllArgsConstructor
    public static class Item {
        private final String status;
        private final String reservationName;
        private final String phoneNumber;
        private final Long reservationId;
        private final LocalDateTime reservationTime;
        private final String className;
        private final Integer guestCount;
        private final String memo;
    }
}
