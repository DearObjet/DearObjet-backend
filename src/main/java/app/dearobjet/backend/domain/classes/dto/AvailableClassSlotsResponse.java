package app.dearobjet.backend.domain.classes.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AvailableClassSlotsResponse(
        LocalDate date,
        String openTime,
        String closeTime,
        List<Slot> slots
) {

    public record Slot(
            Long sessionId,
            String time,
            Integer remainingCapacity,
            boolean available
    ) {
    }
}
