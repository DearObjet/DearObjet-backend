package app.dearobjet.backend.domain.user.controller;

import app.dearobjet.backend.domain.user.dto.CompleteSignupRequest;
import app.dearobjet.backend.domain.user.service.UserService;
import app.dearobjet.backend.global.auth.jwt.JwtProvider;
import app.dearobjet.backend.global.auth.security.CustomUserDetails;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final JwtProvider jwtProvider;

    /**
     * 추가 회원가입 (TEMP → CUSTOMER / ARTIST / SHOP)
     */
    @PostMapping("/complete")
    public ResponseEntity<Void> completeSignup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CompleteSignupRequest request,
            HttpServletResponse response
    ) {
        Long userId = userDetails.getUserId();

        // 추가 회원가입
        userService.completeSignup(
                userId,
                request.getName(),
                request.getEmail(),
                request.getPhoneNumber(),
                request.getSmsAgreement(),
                request.getMarketingAgreement(),
                request.getRole()
        );

        // role 변경되었으므로 JWT 재발급
        String newAccessToken =
                jwtProvider.createAccessToken(userId, request.getRole());

        Cookie cookie = new Cookie("ACCESS_TOKEN", newAccessToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // dev 환경
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60);

        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }
}