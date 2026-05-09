package app.dearobjet.backend.global.auth.service;

import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.enums.UserStatus;
import app.dearobjet.backend.domain.user.repository.UserRepository;
import app.dearobjet.backend.global.auth.dto.RefreshResult;
import app.dearobjet.backend.global.auth.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRedisService refreshTokenRedisService;
    private final UserRepository userRepository;

    public RefreshResult refresh(String refreshToken) {

        // 1. refresh token 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new IllegalStateException("INVALID_REFRESH_TOKEN");
        }

        // 2. Redis에서 userId 조회
        Long userId = refreshTokenRedisService.getUserId(refreshToken);

        // 3. RTR: 기존 refresh 폐기
        refreshTokenRedisService.delete(refreshToken);

        // 4. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow();

        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("INACTIVE_USER");
        }

        // 5. 새 토큰 발급
        String newAccessToken =
                jwtProvider.createAccessToken(user.getId(), user.getRole());

        String newRefreshToken =
                jwtProvider.createRefreshToken(user.getId());

        // 6. 새 refresh Redis 저장
        refreshTokenRedisService.save(
                newRefreshToken,
                user.getId(),
                jwtProvider.getRefreshTokenExpiration()
        );

        return new RefreshResult(newAccessToken, newRefreshToken);
    }
}
