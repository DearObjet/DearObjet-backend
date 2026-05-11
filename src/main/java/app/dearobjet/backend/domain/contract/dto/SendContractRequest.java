package app.dearobjet.backend.domain.contract.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class SendContractRequest {

    @NotBlank(message = "소품샵 상호명은 필수입니다.")
    private String shopBusinessName;

    @NotBlank(message = "소품샵 대표자는 필수입니다.")
    private String shopOwnerName;

    @NotBlank(message = "소품샵 사업자등록번호는 필수입니다.")
    private String shopBusinessNumber;

    @NotBlank(message = "소품샵 주소는 필수입니다.")
    private String shopAddress;

    @NotBlank(message = "소품샵 연락처는 필수입니다.")
    private String shopContact;

    @NotNull(message = "계약 시작일은 필수입니다.")
    private LocalDate contractStartDate;

    @NotNull(message = "계약 종료일은 필수입니다.")
    private LocalDate contractEndDate;

    @NotNull(message = "수수료율은 필수입니다.")
    @DecimalMin(value = "0.0", inclusive = false, message = "수수료율은 0보다 커야 합니다.")
    private BigDecimal commissionRate;

    @NotNull(message = "정산 기준일은 필수입니다.")
    @Min(value = 1, message = "정산 기준일은 1일 이상이어야 합니다.")
    @Max(value = 31, message = "정산 기준일은 31일 이하여야 합니다.")
    private Integer settlementDay;

    @NotNull(message = "지급일은 필수입니다.")
    @Min(value = 1, message = "지급일은 1일 이상이어야 합니다.")
    @Max(value = 31, message = "지급일은 31일 이하여야 합니다.")
    private Integer paymentDay;

    @NotNull(message = "계약일은 필수입니다.")
    private LocalDate contractDate;

    @NotBlank(message = "서명 상호명은 필수입니다.")
    private String shopSignatureBusinessName;

    @NotBlank(message = "서명 대표자는 필수입니다.")
    private String shopSignatureOwnerName;
}
