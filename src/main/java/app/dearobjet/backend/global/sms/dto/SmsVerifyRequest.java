package app.dearobjet.backend.global.sms.dto;

import lombok.Getter;

@Getter
public class SmsVerifyRequest {
    private String phoneNumber;
    private String code;
}
