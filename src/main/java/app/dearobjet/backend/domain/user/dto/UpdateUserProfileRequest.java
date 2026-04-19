package app.dearobjet.backend.domain.user.dto;

public record UpdateUserProfileRequest(
        String name,
        String phoneNumber,
        Boolean smsAgreement,
        Boolean marketingAgreement
) {
}
