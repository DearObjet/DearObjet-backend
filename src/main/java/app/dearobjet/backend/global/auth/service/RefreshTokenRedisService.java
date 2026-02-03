package app.dearobjet.backend.global.auth.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenRedisService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "refresh:";

    public void save(String refreshToken, Long userId, long ttlMillis) {
        redisTemplate.opsForValue().set(
                PREFIX + refreshToken,
                String.valueOf(userId),
                ttlMillis,
                TimeUnit.MILLISECONDS
        );
    }

    public Long getUserId(String refreshToken) {
        String value = redisTemplate.opsForValue().get(PREFIX + refreshToken);
        if (value == null) {
            throw new RuntimeException("refresh token not found");
        }
        return Long.valueOf(value);
    }

    public void delete(String refreshToken) {
        redisTemplate.delete(PREFIX + refreshToken);
    }
}
