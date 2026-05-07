package app.dearobjet.backend.domain.artist.controller;

import app.dearobjet.backend.domain.artist.dto.ArtistProductItemResponse;
import app.dearobjet.backend.domain.artist.dto.ArtistProductListResponse;
import app.dearobjet.backend.domain.artist.dto.ArtistProductMemoResponse;
import app.dearobjet.backend.domain.artist.dto.CreateArtistProductRequest;
import app.dearobjet.backend.domain.artist.dto.UpdateArtistProductMemoRequest;
import app.dearobjet.backend.domain.artist.dto.UpdateArtistProductRequest;
import app.dearobjet.backend.domain.artist.dto.UpdateArtistProductStockRequest;
import app.dearobjet.backend.domain.artist.dto.UpdateArtistProductStockResponse;
import app.dearobjet.backend.domain.artist.service.ArtistProductService;
import app.dearobjet.backend.global.api.ApiResponse;
import app.dearobjet.backend.global.auth.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/artist/products")
public class ArtistProductController {

    private final ArtistProductService artistProductService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ArtistProductItemResponse> createProduct(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @Valid @RequestPart("request") CreateArtistProductRequest request,
            @RequestPart("productImage") MultipartFile productImage
    ) {
        return ApiResponse.of(
                artistProductService.createProduct(resolveUserId(userDetails, userId), request, productImage)
        );
    }

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

    @GetMapping("/{productId}/memo")
    public ApiResponse<ArtistProductMemoResponse> getProductMemo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @PathVariable Long productId
    ) {
        return ApiResponse.of(
                artistProductService.getProductMemo(resolveUserId(userDetails, userId), productId)
        );
    }

    @PatchMapping("/{productId}/memo")
    public ApiResponse<ArtistProductMemoResponse> updateProductMemo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateArtistProductMemoRequest request
    ) {
        return ApiResponse.of(
                artistProductService.updateProductMemo(resolveUserId(userDetails, userId), productId, request)
        );
    }

    @DeleteMapping("/{productId}/memo")
    public ApiResponse<ArtistProductMemoResponse> deleteProductMemo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @PathVariable Long productId
    ) {
        return ApiResponse.of(
                artistProductService.deleteProductMemo(resolveUserId(userDetails, userId), productId)
        );
    }

    @PutMapping(value = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ArtistProductItemResponse> updateProduct(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @PathVariable Long productId,
            @Valid @RequestPart("request") UpdateArtistProductRequest request,
            @RequestPart(value = "productImage", required = false) MultipartFile productImage
    ) {
        return ApiResponse.of(
                artistProductService.updateProduct(
                        resolveUserId(userDetails, userId),
                        productId,
                        request,
                        productImage
                )
        );
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
