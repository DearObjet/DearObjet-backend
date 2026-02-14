package app.dearobjet.backend.domain.user.dto;

import lombok.Getter;

@Getter
public class ShopSignupRequest {

    private String name;
    private String email;
    private String phoneNumber;
    private Boolean smsAgreement;
    private Boolean marketingAgreement;

    // 사업자 정보
    private String businessNumber;
    private String businessName;
    private String ownerName;

    // shop 정보
    private String shopName;
    private String shopDescription;
    private String address;
}