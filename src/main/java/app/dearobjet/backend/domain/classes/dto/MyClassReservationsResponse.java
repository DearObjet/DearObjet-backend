package app.dearobjet.backend.domain.classes.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MyClassReservationsResponse(
        List<ReservationSummary> currentReservations,
        List<ReservationSummary> pastReservations
) {
    public record ReservationSummary(
            Long reservationNumber,
            String status,
            String reservationStore,
            LocalDateTime reservationTime,
            String className
    ) {
    }
}
