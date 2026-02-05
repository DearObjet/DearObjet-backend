package app.dearobjet.backend.global.sms.dto;
import lombok.Getter;

@Getter
public class SmsSendRequest {
    private String phoneNumber;
    private String message;
}
