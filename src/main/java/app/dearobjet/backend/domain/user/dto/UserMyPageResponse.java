package app.dearobjet.backend.domain.user.dto;

import app.dearobjet.backend.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserMyPageResponse {

    private String name;
    private String phoneNumber;
    private String profileImage;
    private Boolean smsAgreement;
    private Boolean marketingAgreement;

    public static UserMyPageResponse from(User user) {
        return UserMyPageResponse.builder()
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .profileImage(user.getProfileImage())
                .smsAgreement(user.getSmsAgreement())
                .marketingAgreement(user.getMarketingAgreement())
                .build();
    }
}
