package app.dearobjet.backend.domain.shop.dto;

import java.util.List;

public record ShopReviewListResponse(
        List<ShopReviewResponse> items,
        Long nextCursorId,
        boolean hasNext
) {
}
