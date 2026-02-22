package app.dearobjet.backend.domain.user.controller;

import app.dearobjet.backend.domain.user.dto.BusinessSignupRequest;
import app.dearobjet.backend.domain.user.dto.UserSignupRequest;
import app.dearobjet.backend.domain.user.service.UserService;
import app.dearobjet.backend.global.api.ApiResponse;
import app.dearobjet.backend.global.auth.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "User", description = "사용자 가입 API")
public class UserController {

    private final UserService userService;

    // CUSTOMER 가입 완료
    @PostMapping("/complete")
    @Operation(summary = "일반 사용자 가입")
    public ResponseEntity<ApiResponse<Void>> completeCustomerSignup(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserSignupRequest request
    ) {
        userService.completeSignup(
                userDetails.getUserId(),
                request
        );

        return ResponseEntity.ok(ApiResponse.of(null));
    }

    // ARTIST 가입 완료
    @PostMapping(value = "/complete/artist", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "작가 사용자 가입")
    public ResponseEntity<ApiResponse<Void>> completeArtistSignup(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestPart("request") BusinessSignupRequest request,
            @RequestPart("businessLicenseFile") MultipartFile businessLicenseFile
    ) {
        userService.completeArtistSignup(
                userDetails.getUserId(),
                request,
                businessLicenseFile
        );

        return ResponseEntity.ok(ApiResponse.of(null));
    }

    // SHOP 가입 완료
    @PostMapping(value = "/complete/shop", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "상점 사용자 가입")
    public ResponseEntity<ApiResponse<Void>> completeShopSignup(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestPart("request") BusinessSignupRequest request,
            @RequestPart("businessLicenseFile") MultipartFile businessLicenseFile
    ) {
        userService.completeShopSignup(
                userDetails.getUserId(),
                request,
                businessLicenseFile
        );

        return ResponseEntity.ok(ApiResponse.of(null));
    }
}
