package app.dearobjet.backend.domain.classes.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AvailableClassSlotsResponse(
        Long classId,
        LocalDate date,
        String openTime,
        String closeTime,
        List<Slot> slots
) {

    public record Slot(
            Long sessionId,
            LocalDateTime reservationTime,
            String label,
            Integer remainingCapacity
    ) {
    }
}
