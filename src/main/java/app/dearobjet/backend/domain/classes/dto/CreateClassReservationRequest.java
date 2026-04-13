package app.dearobjet.backend.domain.classes.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateClassReservationRequest(
        @NotNull Long sessionId,
        @NotNull @Min(1) Integer guestCount,
        @NotBlank String reservationName,
        String memo
) {
}
