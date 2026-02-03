package app.dearobjet.backend.global.auth.oauth;

import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.service.UserService;
import app.dearobjet.backend.global.auth.jwt.JwtProvider;
import app.dearobjet.backend.global.auth.refresh.RefreshTokenRedisService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
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
        String refreshToken = jwtProvider.createRefreshToken(user.getUserId());

        // Redis 저장
        refreshTokenRedisService.save(
                refreshToken,
                user.getUserId(),
                jwtProvider.getRefreshTokenExpiration()
        );

        // refresh token을 HttpOnly 쿠키로 저장
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true); // https 환경
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(14 * 24 * 60 * 60); // 14일

        response.addCookie(refreshCookie);

        // access는 여기서 주지 않는다
        // 프론트에서 /auth/token/refresh 호출하게 함
        response.sendRedirect("http://localhost:5173/oauth/callback");
    }
}
