package app.dearobjet.backend.domain.shop;

import app.dearobjet.backend.domain.classes.ClassesService;
import app.dearobjet.backend.domain.classes.dto.ClassListResponse;
import app.dearobjet.backend.domain.shop.dto.UpdateBusinessHoursRequest;
import app.dearobjet.backend.domain.shop.dto.ShopBusinessHoursResponse;
import app.dearobjet.backend.domain.shop.service.ShopService;
import app.dearobjet.backend.domain.shop.dto.ShopDetailResponse;
import app.dearobjet.backend.domain.shop.service.ShopMapService;
import app.dearobjet.backend.global.api.ApiResponse;
import app.dearobjet.backend.global.auth.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shops")
public class ShopController {

    private final ShopMapService shopMapService;
    private final ShopService shopService;
    private final ClassesService classesService;

    @GetMapping("/{shopId}")
    public ApiResponse<ShopDetailResponse> getShopDetail(@PathVariable Long shopId) {
        return ApiResponse.of(shopMapService.getShopDetail(shopId));
    }

    @GetMapping("/{shopId}/classes")
    public ApiResponse<ClassListResponse> getShopClasses(
            @PathVariable Long shopId,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.of(classesService.getClassesByShop(shopId, page, size));
    }

    @PatchMapping("/me/business-hours")
    public ApiResponse<ShopBusinessHoursResponse> updateBusinessHours(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateBusinessHoursRequest request
    ) {
        return ApiResponse.of(shopService.updateBusinessHours(userDetails.getUserId(), request));
    }
}
