package app.dearobjet.backend.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserSignupRequest {

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    private String email;

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(
            regexp = "^[0-9]{9,11}$",
            message = "전화번호 형식이 올바르지 않습니다. 숫자만 입력하세요."
    )
    private String phoneNumber;

    @NotNull(message = "SMS 수신 동의 여부는 필수입니다.")
    private Boolean smsAgreement;

    private Boolean marketingAgreement;
}
