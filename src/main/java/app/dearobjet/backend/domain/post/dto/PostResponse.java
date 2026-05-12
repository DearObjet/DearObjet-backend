package app.dearobjet.backend.domain.post.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
        Long postId,
        Long userId,
        String userName,
        String profileUrl,
        String content,
        List<String> imageUrls,
        Boolean isPublic,
        LocalDateTime createdAt
) {
}
