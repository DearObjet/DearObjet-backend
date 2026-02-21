package app.dearobjet.backend.domain.user.dto;

import app.dearobjet.backend.domain.user.enums.BusinessCategory;
import app.dearobjet.backend.domain.user.enums.BusinessType;
import app.dearobjet.backend.domain.user.enums.Specialty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(
            regexp = "^[0-9]{9,11}$",
            message = "전화번호 형식이 올바르지 않습니다. 숫자만 입력하세요."
    )
    private String phoneNumber;

    @NotNull(message = "SMS 수신 동의 여부는 필수입니다.")
    private Boolean smsAgreement;
    private Boolean marketingAgreement;

    // 사업자 필드
    @NotBlank(message = "사업자등록번호는 필수입니다.")
    private String businessNumber;

    @NotBlank(message = "상호명은 필수입니다.")
    private String businessName;

    @NotBlank(message = "대표자명은 필수입니다.")
    private String ownerName;

    @NotBlank(message = "사업장 주소는 필수입니다.")
    private String businessAddress;

    private String businessLicenseUrl;

    @NotNull(message = "업태는 필수입니다.")
    private BusinessType businessType;

    @NotNull(message = "종목은 필수입니다.")
    private BusinessCategory businessCategory;

    @NotNull(message = "전문 분야는 필수입니다.")
    private Specialty specialty;

    private Boolean reviewDataAgreement;
}
