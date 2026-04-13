package app.dearobjet.backend.domain.classes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ClassListResponse {
    private final List<Item> items;
    private final int page;
    private final int totalPages;

    @Getter
    @AllArgsConstructor
    public static class Item {
        private final Long classId;
        private final String className;
        private final String classDescription;
        private final String firstImageUrl;
        private final Integer maxCapacity;
        private final String notes;
    }
}
