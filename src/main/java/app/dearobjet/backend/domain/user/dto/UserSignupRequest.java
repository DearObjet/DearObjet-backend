package app.dearobjet.backend.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserSignupRequest {

    private String name;
    private String email;
    private String phoneNumber;
    private Boolean smsAgreement;
    private Boolean marketingAgreement;
}
