package app.dearobjet.backend.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserSignupRequest {

    @Schema(description = "사용자 이름", example = "홍길동")
    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "전화번호(숫자만)", example = "01012345678")
    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(
            regexp = "^[0-9]{9,11}$",
            message = "전화번호 형식이 올바르지 않습니다. 숫자만 입력하세요."
    )
    private String phoneNumber;

    @Schema(description = "SMS 수신 동의", example = "true")
    @NotNull(message = "SMS 수신 동의 여부는 필수입니다.")
    private Boolean smsAgreement;

    @Schema(description = "마케팅 수신 동의", example = "false")
    private Boolean marketingAgreement;
}
