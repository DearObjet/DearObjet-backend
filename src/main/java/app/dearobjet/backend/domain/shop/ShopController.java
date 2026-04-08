package app.dearobjet.backend.domain.shop;

import app.dearobjet.backend.domain.shop.dto.ShopDetailResponse;
import app.dearobjet.backend.domain.shop.service.ShopMapService;
import app.dearobjet.backend.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shops")
public class ShopController {

    private final ShopMapService shopMapService;

    @GetMapping("/{shopId}")
    public ApiResponse<ShopDetailResponse> getShopDetail(@PathVariable Long shopId) {
        return ApiResponse.of(shopMapService.getShopDetail(shopId));
    }
}
