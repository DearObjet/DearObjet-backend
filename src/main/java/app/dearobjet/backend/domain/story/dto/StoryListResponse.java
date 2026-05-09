package app.dearobjet.backend.domain.story.dto;

import java.time.LocalDateTime;
import java.util.List;

public record StoryListResponse(
        List<Item> items,
        int page,
        int totalPages
) {
    public record Item(
            Long storyId,
            String thumbnailImageUrl,
            String title,
            String content,
            LocalDateTime createdAt
    ) {
    }
}
