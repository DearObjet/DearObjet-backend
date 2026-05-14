package app.dearobjet.backend.domain.classes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ClassReservationListResponse {
    private final long reservationCount;
    private final List<Item> items;
    private final int page;
    private final int totalPages;

    @Getter
    @AllArgsConstructor
    @Schema(name = "ClassReservationListItem")
    public static class Item {
        private final String status;
        private final String reserverName;
        private final String phoneNumber;
        private final Long reservationId;
        private final LocalDateTime usageDateTime;
        private final String className;
    }
}
