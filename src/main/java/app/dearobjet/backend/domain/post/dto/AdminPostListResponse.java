package app.dearobjet.backend.domain.post.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AdminPostListResponse(
        List<Item> items,
        int page,
        int totalPages,
        long totalCount
) {
    public record Item(
            Long postId,
            String preview,
            String authorName,
            LocalDateTime createdAt,
            boolean blinded
    ) {
    }
}
