package app.dearobjet.backend.domain.shop.service;

import app.dearobjet.backend.domain.shop.client.KakaoLocalClient;
import app.dearobjet.backend.domain.shop.dto.ShopDetailResponse;
import app.dearobjet.backend.domain.shop.dto.ShopMapItemResponse;
import app.dearobjet.backend.domain.shop.dto.ShopMapResponse;
import app.dearobjet.backend.domain.shop.entity.Shop;
import app.dearobjet.backend.domain.user.repository.ShopRepository;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.ErrorCode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShopMapService {

    private final ShopRepository shopRepository;
    private final KakaoLocalClient kakaoLocalClient;

    // 같은 주소를 반복 지오코딩하지 않도록 성공한 좌표만 메모리에 보관한다.
    private final Map<String, ShopCoordinate> coordinateCache = new ConcurrentHashMap<>();

    public ShopMapResponse getShopMarkers() {
        List<ShopMapItemResponse> markers = loadShopMarkers();
        log.info("Loaded shop markers count={}", markers.size());
        return new ShopMapResponse(markers);
    }

    public ShopDetailResponse getShopDetail(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND));
        return ShopDetailResponse.from(shop);
    }

    private List<ShopMapItemResponse> loadShopMarkers() {
        List<Shop> shops = shopRepository.findAllByBusinessAddressIsNotNullAndBusinessAddressNot("");
        log.info("Fetched shops with address count={}", shops.size());

        return shops
                .stream()
                .peek(shop -> log.info(
                        "Preparing shop for map shopId={}, address={}",
                        shop.getShopId(),
                        shop.getBusinessAddress()
                ))
                .map(this::toShopMapItem)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<ShopMapItemResponse> toShopMapItem(Shop shop) {
        if (!StringUtils.hasText(shop.getBusinessAddress())) {
            return Optional.empty();
        }

        log.info("Start geocoding shopId={}, address={}", shop.getShopId(), shop.getBusinessAddress());

        // 이미 좌표를 구한 주소면 외부 API를 다시 부르지 않는다.
        ShopCoordinate cachedCoordinate = coordinateCache.get(shop.getBusinessAddress());
        if (cachedCoordinate != null) {
            log.info(
                    "Using cached coordinate shopId={}, lat={}, lng={}",
                    shop.getShopId(),
                    cachedCoordinate.latitude(),
                    cachedCoordinate.longitude()
            );
            return Optional.of(toShopMapItemResponse(shop, cachedCoordinate));
        }

        Optional<ShopCoordinate> coordinate = lookupCoordinate(shop.getBusinessAddress());

        return coordinate.map(value -> {
            // 실패 결과는 캐시하지 않고, 성공한 좌표만 다음 요청에 재사용한다.
            coordinateCache.put(shop.getBusinessAddress(), value);
            log.info(
                    "Geocoding success shopId={}, lat={}, lng={}",
                    shop.getShopId(),
                    value.latitude(),
                    value.longitude()
            );
            return toShopMapItemResponse(shop, value);
        });
    }

    private Optional<ShopCoordinate> lookupCoordinate(String address) {
        try {
            return Optional.ofNullable(kakaoLocalClient.searchAddress(address));
        } catch (RuntimeException exception) {
            log.error("Failed to geocode shop address={}", address, exception);
            return Optional.empty();
        }
    }

    private ShopMapItemResponse toShopMapItemResponse(Shop shop, ShopCoordinate coordinate) {
        // 프론트가 마커를 그릴 때 바로 쓸 수 있는 형태로 응답을 만든다.
        return ShopMapItemResponse.builder()
                .shopId(shop.getShopId())
                .shopName(shop.getShopName())
                .businessName(shop.getBusinessName())
                .businessAddress(shop.getBusinessAddress())
                .latitude(coordinate.latitude())
                .longitude(coordinate.longitude())
                .build();
    }
}
