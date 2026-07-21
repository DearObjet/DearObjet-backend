package app.dearobjet.backend.domain.classes.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AdminClassListResponse(
        List<ClassSummary> classes,
        int page,
        int totalPages,
        long totalCount
) {
    public record ClassSummary(
            Long classId,
            String className,
            String shopName,
            LocalDateTime createdAt,
            boolean blinded
    ) {
    }
}
