package app.dearobjet.backend.domain.classes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ClassResponse {
    private final Long classId;
    private final String className;
    private final String classDescription;
    private final List<String> classImageUrls;
    private final Double price;
    private final Integer maxCapacity;
    private final String notes;
}
