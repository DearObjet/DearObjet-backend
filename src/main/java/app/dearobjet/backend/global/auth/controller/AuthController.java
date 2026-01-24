package app.dearobjet.backend.global.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @PostMapping("/auth/logout")
    public void logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("ACCESS_TOKEN", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0); // 즉시 삭제
        response.addCookie(cookie);
    }
}
