package app.dearobjet.backend.domain.shop;

import app.dearobjet.backend.domain.shop.dto.CreateShopReviewRequest;
import app.dearobjet.backend.domain.shop.dto.ShopReviewListResponse;
import app.dearobjet.backend.domain.shop.dto.ShopReviewResponse;
import app.dearobjet.backend.domain.shop.dto.UpdateShopReviewRequest;
import app.dearobjet.backend.domain.shop.service.ShopReviewService;
import app.dearobjet.backend.global.api.ApiResponse;
import app.dearobjet.backend.global.auth.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shops/{shopId}/reviews")
public class ShopReviewController {

    private final ShopReviewService shopReviewService;

    @GetMapping
    public ApiResponse<ShopReviewListResponse> getShopReviews(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long shopId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long currentUserId = userDetails == null ? null : userDetails.getUserId();
        return ApiResponse.of(shopReviewService.getReviews(currentUserId, shopId, cursorId, size));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ShopReviewResponse> createShopReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long shopId,
            @Valid @RequestPart("request") CreateShopReviewRequest request,
            @RequestPart("image") MultipartFile image
    ) {
        return ApiResponse.of(shopReviewService.createReview(userDetails.getUserId(), shopId, request, image));
    }

    @PutMapping(value = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ShopReviewResponse> updateShopReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long shopId,
            @PathVariable Long reviewId,
            @Valid @RequestPart("request") UpdateShopReviewRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return ApiResponse.of(shopReviewService.updateReview(userDetails.getUserId(), shopId, reviewId, request, image));
    }

    @DeleteMapping("/{reviewId}")
    public ApiResponse<Void> deleteShopReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long shopId,
            @PathVariable Long reviewId
    ) {
        shopReviewService.deleteReview(userDetails.getUserId(), shopId, reviewId);
        return ApiResponse.of(null);
    }
}
