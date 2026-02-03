package app.dearobjet.backend.global.auth.oauth;

import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.service.UserService;
import app.dearobjet.backend.global.auth.jwt.JwtProvider;
import app.dearobjet.backend.global.auth.service.RefreshTokenRedisService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRedisService refreshTokenRedisService;
    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String socialId = oAuth2User.getAttribute("id").toString();

        User user = userService.getOrCreateKakaoUser(socialId);

        // refresh token 발급
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        // Redis 저장
        refreshTokenRedisService.save(
                refreshToken,
                user.getId(),
                jwtProvider.getRefreshTokenExpiration()
        );

        // refresh token을 HttpOnly 쿠키로 저장
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true); // https 환경
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(14 * 24 * 60 * 60); // 14일

        response.addCookie(refreshCookie);

        // 추가 회원가입 필요 여부 판단
        boolean signupRequired = user.getRole() == Role.TEMP;

        String redirectUrl = frontendBaseUrl
                + "/oauth/callback"
                + (signupRequired ? "?signup=required" : "?signup=completed");

        response.sendRedirect(redirectUrl);
    }
}
