package app.dearobjet.backend.domain.shop;

import app.dearobjet.backend.domain.shop.dto.ShopMapResponse;
import app.dearobjet.backend.domain.shop.service.ShopMapService;
import app.dearobjet.backend.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shops/map")
public class ShopMapController {

    private final ShopMapService shopMapService;

    @GetMapping
    public ApiResponse<ShopMapResponse> getShopMarkers() {
        return ApiResponse.of(shopMapService.getShopMarkers());
    }
}
