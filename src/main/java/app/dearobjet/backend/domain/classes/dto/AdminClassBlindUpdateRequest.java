package app.dearobjet.backend.domain.classes.dto;

import jakarta.validation.constraints.NotNull;

public record AdminClassBlindUpdateRequest(
        @NotNull Boolean blinded
) {
}
