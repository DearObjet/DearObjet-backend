package app.dearobjet.backend.domain.contract.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SubmitArtistContractRequest {

    @NotBlank(message = "작가명은 필수입니다.")
    private String artistName;

    private String artistBusinessNumber;

    @NotBlank(message = "작가 주소는 필수입니다.")
    private String artistAddress;

    @NotBlank(message = "작가 연락처는 필수입니다.")
    private String artistContact;

    @NotBlank(message = "은행명은 필수입니다.")
    private String artistBankName;

    @NotBlank(message = "예금주는 필수입니다.")
    private String artistAccountHolder;

    @NotBlank(message = "계좌번호는 필수입니다.")
    private String artistAccountNumber;

    @NotBlank(message = "작가 서명은 필수입니다.")
    private String artistSignatureName;
}
