package app.dearobjet.backend.domain.post.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
        Long postId,
        Long userId,
        String userName,
        String title,
        String content,
        List<String> imageUrls,
        Integer viewCount,
        Integer likeCount,
        Boolean isPublic,
        LocalDateTime createdAt
) {
}
