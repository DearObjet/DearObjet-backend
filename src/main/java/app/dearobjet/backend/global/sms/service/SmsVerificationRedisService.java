package app.dearobjet.backend.global.sms.service;

import app.dearobjet.backend.global.sms.policy.SmsPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class SmsVerificationRedisService {

    private final StringRedisTemplate redisTemplate;

    private static final Duration TTL = Duration.ofMinutes(3);

    public void saveCode(String phoneNumber, String code) {
        redisTemplate.opsForValue().set(
                key("verify", phoneNumber),
                code,
                Duration.ofMinutes(SmsPolicy.CODE_TTL_MINUTES)
        );
    }

    public String getCode(String phoneNumber) {
        return redisTemplate.opsForValue().get(key("verify", phoneNumber));
    }

    public void deleteCode(String phone) {
        redisTemplate.delete(key("verify", phone));
        redisTemplate.delete(key("attempt", phone));
    }

    public int increaseAttempt(String phone) {
        Long count = redisTemplate.opsForValue()
                .increment(key("attempt", phone));

        redisTemplate.expire(
                key("attempt", phone),
                Duration.ofMinutes(SmsPolicy.CODE_TTL_MINUTES)
        );

        return count.intValue();
    }

    public boolean isCooldown(String phoneNumber) {
        return redisTemplate.hasKey(key("cooldown", phoneNumber));
    }

    public void applyCooldown(String phone) {
        redisTemplate.opsForValue().set(
                key("cooldown", phone),
                "1",
                Duration.ofSeconds(SmsPolicy.RESEND_COOLDOWN_SECONDS)
        );
    }

    private String key(String type, String phoneNumber) {
        return "sms:" + type + ":" + phoneNumber;
    }
}

