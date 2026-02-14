package app.dearobjet.backend.domain.user.dto;

import app.dearobjet.backend.domain.user.enums.Specialty;
import lombok.Getter;

@Getter
public class ArtistSignupRequest {

    // 공통 필드
    private String name;
    private String email;
    private String phoneNumber;
    private Boolean smsAgreement;
    private Boolean marketingAgreement;

    // 아티스트 전용 필드
    private String businessNumber;
    private String businessName;
    private String ownerName;
    private String bio;
    private String portfolioUrl;
    private Specialty specialty;
}

