package app.dearobjet.backend.global.sms.service;

import app.dearobjet.backend.global.sms.policy.SmsPolicy;
import app.dearobjet.backend.global.sms.util.VerificationCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmsAuthService {

    private final SolapiSmsService smsService;
    private final SmsVerificationRedisService redisService;

    public void sendVerificationCode(String phone) {

        if (redisService.isCooldown(phone)) {
            throw new IllegalStateException("잠시 후 다시 요청해주세요.");
        }

        String code = VerificationCodeGenerator.generate();

        smsService.sendSms(
                phone,
                "인증번호는 " + code + " 입니다. (3분 이내 입력)"
        );

        redisService.saveCode(phone, code);
        redisService.applyCooldown(phone);
    }

    public void verifyCode(String phone, String inputCode) {

        String savedCode = redisService.getCode(phone);

        if (savedCode == null) {
            throw new IllegalStateException("인증번호가 만료되었거나 요청되지 않았습니다.");
        }

        int attempt = redisService.increaseAttempt(phone);

        if (attempt > SmsPolicy.MAX_ATTEMPT) {
            redisService.deleteCode(phone);
            throw new IllegalStateException("인증 시도 횟수를 초과했습니다.");
        }

        if (!savedCode.equals(inputCode)) {
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }

        redisService.markVerified(phone);
        redisService.deleteCode(phone);
    }

    public void assertVerified(String phone) {
        if (!redisService.isVerified(phone)) {
            throw new IllegalStateException("휴대폰 인증이 필요합니다.");
        }
    }

    public void consumeVerified(String phone) {
        redisService.clearVerified(phone);
    }
}