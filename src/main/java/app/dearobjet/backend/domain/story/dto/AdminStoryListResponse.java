package app.dearobjet.backend.domain.story.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AdminStoryListResponse(
        List<StorySummary> stories,
        int page,
        int totalPages,
        long totalCount
) {
    public record StorySummary(
            Long storyId,
            String title,
            String userName,
            LocalDateTime createdAt,
            boolean blinded
    ) {
    }
}
