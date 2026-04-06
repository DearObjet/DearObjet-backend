package app.dearobjet.backend.domain.classes.dto;

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
    public static class Item {
        private final String status;
        private final String reserverName;
        private final String phoneNumber;
        private final Long reservationNumber;
        private final LocalDateTime usageDateTime;
        private final String className;
    }
}
