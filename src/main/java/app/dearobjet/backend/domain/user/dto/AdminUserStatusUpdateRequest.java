package app.dearobjet.backend.domain.user.dto;

import app.dearobjet.backend.domain.user.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

public record AdminUserStatusUpdateRequest(
        @NotNull UserStatus status
) {
}
