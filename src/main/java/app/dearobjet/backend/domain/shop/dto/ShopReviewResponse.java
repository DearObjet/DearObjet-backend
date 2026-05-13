package app.dearobjet.backend.domain.shop.dto;

import java.time.LocalDateTime;

public record ShopReviewResponse(
        Long reviewId,
        Long userId,
        String authorName,
        String authorProfileUrl,
        String title,
        String content,
        String imageUrl,
        boolean owner,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
