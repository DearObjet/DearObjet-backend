package app.dearobjet.backend.domain.user.dto;

import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.enums.UserStatus;
import java.time.LocalDateTime;
import java.util.List;

public record AdminUserListResponse(
        List<UserSummary> users,
        int page,
        int totalPages,
        long totalCount
) {
    public record UserSummary(
            Long userId,
            String name,
            String email,
            Role role,
            LocalDateTime createdAt,
            UserStatus status
    ) {
    }
}
