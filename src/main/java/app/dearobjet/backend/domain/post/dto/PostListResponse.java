package app.dearobjet.backend.domain.post.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostListResponse(
        List<Item> items,
        int page,
        int totalPages
) {
    public record Item(
            Long postId,
            String thumbnailUrl,
            String authorName,
            String authorProfileUrl,
            LocalDateTime createdAt
    ) {
    }
}
