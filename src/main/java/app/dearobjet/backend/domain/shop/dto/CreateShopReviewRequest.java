package app.dearobjet.backend.domain.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateShopReviewRequest(
        @NotBlank(message = "리뷰 제목은 필수입니다.")
        String title,

        @NotBlank(message = "리뷰 내용은 필수입니다.")
        @Size(max = 150, message = "리뷰 내용은 150자를 초과할 수 없습니다.")
        String content
) {
}
