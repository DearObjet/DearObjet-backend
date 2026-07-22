package app.dearobjet.backend.domain.story.dto;

import java.time.LocalDateTime;

public record AdminStoryDetailResponse(
        Long storyId,
        String title,
        String userName,
        LocalDateTime createdAt,
        boolean blinded,
        String content,
        String thumbnailImageUrl
) {
}
