package app.dearobjet.backend.domain.user.dto;

import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.enums.UserStatus;
import java.time.LocalDateTime;

public record AdminUserDetailResponse(
        Long userId,
        String name,
        String email,
        String phoneNumber,
        Role role,
        UserStatus status,
        String profileUrl,
        String socialId,
        Boolean smsAgreement,
        Boolean marketingAgreement,
        LocalDateTime withdrawalRequestedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminUserDetailResponse from(User user) {
        return new AdminUserDetailResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getUserStatus(),
                user.getProfileUrl(),
                user.getSocialId(),
                user.getSmsAgreement(),
                user.getMarketingAgreement(),
                user.getWithdrawalRequestedAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
