package app.dearobjet.backend.domain.story.dto;

import jakarta.validation.constraints.NotNull;

public record AdminStoryBlindUpdateRequest(
        @NotNull Boolean blinded
) {
}
