package app.dearobjet.backend.global.sms.controller;

import app.dearobjet.backend.global.api.ApiResponse;
import app.dearobjet.backend.global.sms.dto.SmsSendRequest;
import app.dearobjet.backend.global.sms.dto.SmsSendResponse;
import app.dearobjet.backend.global.sms.dto.SmsVerifyRequest;
import app.dearobjet.backend.global.sms.dto.SmsVerifyResponse;
import app.dearobjet.backend.global.sms.policy.SmsPolicy;
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
    public ApiResponse<SmsSendResponse> sendSms(@RequestBody SmsSendRequest request) {

        smsAuthService.sendVerificationCode(request.getPhoneNumber());

        return ApiResponse.of(
                new SmsSendResponse(
                        request.getPhoneNumber(),
                        SmsPolicy.CODE_TTL_MINUTES * 60
                )
        );
    }

    @PostMapping("/verify")
    public ApiResponse<SmsVerifyResponse> verify(@RequestBody SmsVerifyRequest request) {

        smsAuthService.verifyCode(
                request.getPhoneNumber(),
                request.getCode()
        );

        return ApiResponse.of(new SmsVerifyResponse(true));
    }
}