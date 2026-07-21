package app.dearobjet.backend.domain.classes.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AdminClassDetailResponse(
        Long classId,
        String className,
        String shopName,
        LocalDateTime createdAt,
        boolean blinded,
        String classDescription,
        List<String> classImageUrls,
        Double price,
        Integer maxCapacity,
        String notes
) {
}
