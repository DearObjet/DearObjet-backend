package app.dearobjet.backend.global.auth.controller;

import app.dearobjet.backend.global.auth.service.RefreshTokenRedisService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final RefreshTokenRedisService refreshTokenRedisService;

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request,
                                       HttpServletResponse response) {

        // 1. refresh 쿠키에서 값 추출
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        // 2. Redis에서 refresh 삭제
        if (refreshToken != null) {
            refreshTokenRedisService.delete(refreshToken);
        }

        // 3. refresh 쿠키 삭제
        Cookie deleteCookie = new Cookie("refreshToken", null);
        deleteCookie.setHttpOnly(true);
        deleteCookie.setSecure(true); // https 환경
        deleteCookie.setPath("/");
        deleteCookie.setMaxAge(0);

        response.addCookie(deleteCookie);

        return ResponseEntity.noContent().build();
    }
}
