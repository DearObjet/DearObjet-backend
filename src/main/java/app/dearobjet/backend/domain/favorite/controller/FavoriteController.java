package app.dearobjet.backend.domain.favorite.controller;

import app.dearobjet.backend.domain.favorite.dto.FavoriteShopItemResponse;
import app.dearobjet.backend.domain.favorite.dto.FavoriteShopListResponse;
import app.dearobjet.backend.domain.favorite.service.FavoriteService;
import app.dearobjet.backend.global.api.ApiResponse;
import app.dearobjet.backend.global.auth.security.CustomUserDetails;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/shops/{shopId}")
    public ApiResponse<FavoriteShopItemResponse> addShopFavorite(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long shopId
    ) {
        return ApiResponse.of(favoriteService.addShopFavorite(userDetails.getUserId(), shopId));
    }

    @DeleteMapping("/shops/{shopId}")
    public ApiResponse<Void> removeShopFavorite(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long shopId
    ) {
        favoriteService.removeShopFavorite(userDetails.getUserId(), shopId);
        return ApiResponse.of(null);
    }

    @GetMapping("/shops")
    public ApiResponse<FavoriteShopListResponse> getFavoriteShops(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return ApiResponse.of(favoriteService.getFavoriteShops(userDetails.getUserId(), page, size));
    }
}
