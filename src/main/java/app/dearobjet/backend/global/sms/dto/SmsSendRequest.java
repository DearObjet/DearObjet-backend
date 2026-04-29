package app.dearobjet.backend.global.sms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class SmsSendRequest {

    @NotBlank(message = "휴대폰 번호는 필수입니다.")
    @Pattern(
            regexp = "^[0-9]{9,11}$",
            message = "휴대폰 번호 형식이 올바르지 않습니다. 숫자만 입력해주세요."
    )
    private String phoneNumber;
}
