package app.dearobjet.backend.global.auth.controller;

import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.repository.UserRepository;
import app.dearobjet.backend.global.api.ApiResponse;
import app.dearobjet.backend.global.auth.dto.AccessTokenResponse;
import app.dearobjet.backend.global.auth.jwt.JwtProvider;
import app.dearobjet.backend.global.auth.refresh.RefreshTokenRedisService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/token")
@RequiredArgsConstructor
public class TokenController {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRedisService refreshTokenRedisService;
    private final UserRepository userRepository;

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AccessTokenResponse>> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        String refreshToken = extractRefreshToken(request);
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 1. refresh JWT 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. Redis 조회 + userId 확보
        Long userId;
        try {
            userId = refreshTokenRedisService.getUserId(refreshToken);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 3. 기존 refresh 즉시 폐기 (RTR 핵심)
        refreshTokenRedisService.delete(refreshToken);

        // 4. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow();

        // 5. 새 토큰 발급
        String newAccessToken =
                jwtProvider.createAccessToken(user.getUserId(), user.getRole());

        String newRefreshToken =
                jwtProvider.createRefreshToken(user.getUserId());

        // 6. 새 refresh Redis 저장
        long refreshTtlMillis = jwtProvider.getRefreshTokenExpiration();
        refreshTokenRedisService.save(
                newRefreshToken,
                user.getUserId(),
                refreshTtlMillis
        );

        // 7. refresh 쿠키 교체
        Cookie refreshCookie = new Cookie("refreshToken", newRefreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int) (refreshTtlMillis / 1000));
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(
                ApiResponse.of(new AccessTokenResponse(newAccessToken))
        );
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}

