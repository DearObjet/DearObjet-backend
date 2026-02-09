package app.dearobjet.backend.global.sms.controller;

import app.dearobjet.backend.global.sms.dto.SmsSendRequest;
import app.dearobjet.backend.global.sms.dto.SmsVerifyRequest;
import app.dearobjet.backend.global.sms.service.SmsAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/phone-verifications")
public class SmsController {

    private final SmsAuthService smsAuthService;

    @PostMapping("/send")
    public void sendSms(@RequestBody SmsSendRequest request) {
        smsAuthService.sendVerificationCode(request.getPhoneNumber());
    }

    @PostMapping("/verify")
    public void verify(@RequestBody SmsVerifyRequest request) {
        smsAuthService.verifyCode(
                request.getPhoneNumber(),
                request.getCode()
        );
    }
}