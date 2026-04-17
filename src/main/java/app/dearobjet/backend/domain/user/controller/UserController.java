package app.dearobjet.backend.domain.user.controller;

import app.dearobjet.backend.domain.user.dto.BusinessSignupRequest;
import app.dearobjet.backend.domain.user.dto.BusinessProfileDetailResponse;
import app.dearobjet.backend.domain.user.dto.UpdateBusinessProfileRequest;
import app.dearobjet.backend.domain.user.dto.UpdateUserProfileRequest;
import app.dearobjet.backend.domain.user.dto.UserInfoResponse;
import app.dearobjet.backend.domain.user.dto.UserProfileResponse;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "User", description = "사용자 가입 API")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getMyInfo(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.of(userService.getUserInfo(userDetails.getUserId())));
    }

    @GetMapping("/me/profile")
    @Operation(summary = "내 프로필 조회")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.of(userService.getUserProfile(userDetails.getUserId())));
    }

    @GetMapping("/me/business-profile")
    @Operation(summary = "사업자 개인정보 조회")
    public ResponseEntity<ApiResponse<BusinessProfileDetailResponse>> getMyBusinessProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.of(userService.getBusinessProfileDetail(userDetails.getUserId())));
    }

    @PatchMapping(value = "/me/business-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "사업자 개인정보 수정")
    public ResponseEntity<ApiResponse<BusinessProfileDetailResponse>> updateMyBusinessProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestPart("request") UpdateBusinessProfileRequest request,
            @RequestPart(value = "bankbookImage", required = false) MultipartFile bankbookImage
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                userService.updateBusinessProfileDetail(userDetails.getUserId(), request, bankbookImage)
        ));
    }

    @PatchMapping(value = "/me/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "내 프로필 수정")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestPart("request") UpdateUserProfileRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                userService.updateUserProfile(userDetails.getUserId(), request, profileImage)
        ));
    }

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
