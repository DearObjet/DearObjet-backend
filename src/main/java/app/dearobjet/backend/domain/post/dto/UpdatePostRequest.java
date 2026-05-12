package app.dearobjet.backend.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePostRequest(
        @NotBlank(message = "내용은 필수입니다.")
        @Size(max = 500, message = "내용은 500자를 초과할 수 없습니다.")
        String content,

        Boolean isPublic
) {
}
