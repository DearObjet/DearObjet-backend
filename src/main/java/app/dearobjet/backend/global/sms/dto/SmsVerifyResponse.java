package app.dearobjet.backend.global.sms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SmsVerifyResponse {
    private final boolean verified;
}

