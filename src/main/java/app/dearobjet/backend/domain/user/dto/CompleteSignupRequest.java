package app.dearobjet.backend.domain.user.dto;

import app.dearobjet.backend.domain.user.enums.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CompleteSignupRequest {

    private String name;
    private String email;
    private String phoneNumber;
    private Boolean smsAgreement;
    private Boolean marketingAgreement;
    private Role role; // CUSTOMER / ARTIST / SHOP
}
