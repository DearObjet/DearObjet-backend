package app.dearobjet.backend.domain.user.controller;

import app.dearobjet.backend.domain.user.dto.BusinessSignupRequest;
import app.dearobjet.backend.domain.user.dto.CustomerSignupRequest;
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

    // CUSTOMER 가입 완료
    @PostMapping("/complete/customer")
    public ResponseEntity<ApiResponse<Void>> completeCustomerSignup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CustomerSignupRequest request
    ) {
        userService.completeCustomerSignup(
                userDetails.getUserId(),
                request
        );

        return ResponseEntity.ok(ApiResponse.of(null));
    }

    // ARTIST 가입 완료
    @PostMapping("/complete/artist")
    public ResponseEntity<ApiResponse<Void>> completeArtistSignup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody BusinessSignupRequest request
    ) {
        userService.completeArtistSignup(
                userDetails.getUserId(),
                request
        );

        return ResponseEntity.ok(ApiResponse.of(null));
    }

    // SHOP 가입 완료
    @PostMapping("/complete/shop")
    public ResponseEntity<ApiResponse<Void>> completeShopSignup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody BusinessSignupRequest request
    ) {
        userService.completeArtistSignup(
                userDetails.getUserId(),
                request
        );

        return ResponseEntity.ok(ApiResponse.of(null));
    }
}