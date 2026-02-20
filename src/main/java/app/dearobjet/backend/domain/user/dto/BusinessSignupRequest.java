package app.dearobjet.backend.domain.user.dto;

import app.dearobjet.backend.domain.user.enums.BusinessCategory;
import app.dearobjet.backend.domain.user.enums.BusinessType;
import app.dearobjet.backend.domain.user.enums.Specialty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessSignupRequest {


    // 공통 필드
    private String name;
    private String phoneNumber;

    private Boolean smsAgreement;
    private Boolean marketingAgreement;

    // 사업자 필드
    private String businessNumber;
    private String businessName;
    private String ownerName;
    private String businessAddress;

    private String businessLicenseUrl;

    private BusinessType businessType;
    private BusinessCategory businessCategory;
    private Specialty specialty;
}
