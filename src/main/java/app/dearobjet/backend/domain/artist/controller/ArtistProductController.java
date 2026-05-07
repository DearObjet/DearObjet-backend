package app.dearobjet.backend.domain.artist.controller;

import app.dearobjet.backend.domain.artist.dto.ArtistProductListResponse;
import app.dearobjet.backend.domain.artist.dto.UpdateArtistProductStockRequest;
import app.dearobjet.backend.domain.artist.dto.UpdateArtistProductStockResponse;
import app.dearobjet.backend.domain.artist.service.ArtistProductService;
import app.dearobjet.backend.global.api.ApiResponse;
import app.dearobjet.backend.global.auth.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/artist/products")
public class ArtistProductController {

    private final ArtistProductService artistProductService;

    @GetMapping
    public ApiResponse<ArtistProductListResponse> getProducts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.of(artistProductService.getProducts(resolveUserId(userDetails, userId), page, size));
    }

    @PatchMapping("/stocks")
    public ApiResponse<UpdateArtistProductStockResponse> updateStocks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @Valid @RequestBody UpdateArtistProductStockRequest request
    ) {
        return ApiResponse.of(artistProductService.updateStocks(resolveUserId(userDetails, userId), request));
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<Void> deleteProduct(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @PathVariable Long productId
    ) {
        artistProductService.deleteProduct(resolveUserId(userDetails, userId), productId);
        return ApiResponse.of(null);
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
