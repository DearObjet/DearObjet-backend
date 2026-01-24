package app.dearobjet.backend.global.auth.oauth;

import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.service.UserService;
import app.dearobjet.backend.global.auth.jwt.JwtProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtProvider jwtProvider;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 카카오 고유 ID
        String socialId = oAuth2User.getAttribute("id").toString();

        // 사용자 조회 or 생성 (PENDING)
        User user = userService.getOrCreateKakaoUser(socialId);

        // JWT 발급
        String accessToken = jwtProvider.createAccessToken(user.getUserId());

        Cookie cookie = new Cookie("ACCESS_TOKEN", accessToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // https면 true
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60); // 1시간

        response.addCookie(cookie);

        // 프론트로 전달
        response.sendRedirect(
                "http://localhost:5173/oauth/callback?token=" + accessToken
        );
    }
}

