package app.dearobjet.backend.domain.classes.dto;

public record CreateClassReservationResponse(
        Long reservationId,
        String reservationStatus
) {
}
