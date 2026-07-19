package app.dearobjet.backend.domain.user.dto;

import app.dearobjet.backend.domain.user.enums.UserStatus;

public record AdminUserStatusResponse(
        Long userId,
        UserStatus status
) {
}
