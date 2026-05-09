package app.dearobjet.backend.domain.story.dto;

import java.time.LocalDateTime;

public record StoryResponse(
        Long storyId,
        String thumbnailImageUrl,
        String title,
        String content,
        LocalDateTime createdAt
) {
}
