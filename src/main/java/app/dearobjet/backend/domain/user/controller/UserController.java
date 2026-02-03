package app.dearobjet.backend.domain.user.controller;

import app.dearobjet.backend.domain.user.dto.CompleteSignupRequest;
import app.dearobjet.backend.domain.user.service.UserService;
import app.dearobjet.backend.global.api.ApiResponse;
import app.dearobjet.backend.global.auth.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    /**
     * 추가 회원가입 (TEMP → CUSTOMER / ARTIST / SHOP)
     */
    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<Void>> completeSignup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CompleteSignupRequest request
    ) {
        Long userId = userDetails.getUserId();

        userService.completeSignup(
                userId,
                request.getName(),
                request.getEmail(),
                request.getPhoneNumber(),
                request.getSmsAgreement(),
                request.getMarketingAgreement(),
                request.getRole()
        );

        return ResponseEntity.ok(ApiResponse.of(null));
    }
}