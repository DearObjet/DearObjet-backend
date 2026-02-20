package app.dearobjet.backend.global.sms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SmsSendResponse {
    private final String phoneNumber;
    private final int expiresInSec;
}

