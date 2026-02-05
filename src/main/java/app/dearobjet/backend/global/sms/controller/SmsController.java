package app.dearobjet.backend.global.sms.controller;

import app.dearobjet.backend.global.sms.dto.SmsSendRequest;
import app.dearobjet.backend.global.sms.service.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/sms")
public class SmsController {

    private final SmsService smsService;

    @PostMapping("/send")
    public void sendSms(@RequestBody SmsSendRequest request) {
        smsService.sendSms(request.getPhoneNumber(), request.getMessage());
    }
}