package app.dearobjet.backend.domain.user.dto;

import app.dearobjet.backend.domain.user.entity.User;

public record UserProfileResponse(
        String profileUrl,
        String name,
        String phoneNumber,
        String email,
        Boolean smsAgreement,
        Boolean marketingAgreement
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getProfileUrl(),
                user.getName(),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getSmsAgreement(),
                user.getMarketingAgreement()
        );
    }
}
