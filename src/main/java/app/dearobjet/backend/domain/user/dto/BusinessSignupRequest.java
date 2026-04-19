package app.dearobjet.backend.domain.user.dto;

import app.dearobjet.backend.domain.user.enums.BusinessCategory;
import app.dearobjet.backend.domain.user.enums.BusinessType;
import app.dearobjet.backend.domain.user.enums.Specialty;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "표시 이름", example = "디어오브제")
    private String name;

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

    // 사업자 필드
    @Schema(description = "사업자등록번호", example = "1234567890")
    @NotBlank(message = "사업자등록번호는 필수입니다.")
    private String businessNumber;

    @Schema(description = "상호명", example = "디어오브제 스튜디오")
    @NotBlank(message = "상호명은 필수입니다.")
    private String businessName;

    @Schema(description = "대표자명", example = "홍길동")
    @NotBlank(message = "대표자명은 필수입니다.")
    private String ownerName;

    @Schema(description = "사업장 주소", example = "서울특별시 강남구 테헤란로 123")
    @NotBlank(message = "사업장 주소는 필수입니다.")
    private String businessAddress;

    @Schema(description = "인스타그램 아이디", example = "dearobjet")
    private String instagramId;

    @Schema(description = "업태", example = "SERVICE")
    @NotNull(message = "업태는 필수입니다.")
    private BusinessType businessType;

    @Schema(description = "종목", example = "CRAFT_RETAIL")
    @NotNull(message = "종목은 필수입니다.")
    private BusinessCategory businessCategory;

    @Schema(description = "전문 분야", example = "HANDMADE_CRAFT")
    @NotNull(message = "전문 분야는 필수입니다.")
    private Specialty specialty;

    @Schema(description = "리뷰 데이터 활용 동의", example = "true")
    private Boolean reviewDataAgreement;
}
