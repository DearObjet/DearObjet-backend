package app.dearobjet.backend.domain.artist.controller;

import app.dearobjet.backend.domain.artist.dto.ArtistShipmentProductListResponse;
import app.dearobjet.backend.domain.artist.dto.ArtistShipmentShopListResponse;
import app.dearobjet.backend.domain.artist.service.ArtistShipmentService;
import app.dearobjet.backend.global.api.ApiResponse;
import app.dearobjet.backend.global.auth.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/artist/shipments")
public class ArtistShipmentController {

    private final ArtistShipmentService artistShipmentService;

    @GetMapping("/shops")
    public ApiResponse<ArtistShipmentShopListResponse> getShipmentShops(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId
    ) {
        return ApiResponse.of(artistShipmentService.getShipmentShops(resolveUserId(userDetails, userId)));
    }

    @GetMapping("/shops/{contractId}/products")
    public ApiResponse<ArtistShipmentProductListResponse> getShipmentProducts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @PathVariable Long contractId
    ) {
        return ApiResponse.of(
                artistShipmentService.getShipmentProducts(resolveUserId(userDetails, userId), contractId)
        );
    }

    private Long resolveUserId(CustomUserDetails userDetails, Long userId) {
        if (userDetails != null) {
            return userDetails.getUserId();
        }
        if (userId != null) {
            return userId;
        }
        throw new IllegalArgumentException("인증 정보가 없으면 userId 쿼리 파라미터가 필요합니다.");
    }
}
