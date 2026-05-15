package app.dearobjet.backend.domain.shop.service;

import app.dearobjet.backend.domain.contract.ContractRepository;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import app.dearobjet.backend.domain.favorite.enums.FavoriteTargetType;
import app.dearobjet.backend.domain.favorite.repository.FavoriteRepository;
import app.dearobjet.backend.domain.shop.client.KakaoLocalClient;
import app.dearobjet.backend.domain.shop.dto.ShopContractedArtistListResponse;
import app.dearobjet.backend.domain.shop.dto.ShopGeocodeResponse;
import app.dearobjet.backend.domain.shop.dto.ShopDetailResponse;
import app.dearobjet.backend.domain.shop.dto.ShopMapItemResponse;
import app.dearobjet.backend.domain.shop.dto.ShopMapResponse;
import app.dearobjet.backend.domain.shop.entity.Shop;
import app.dearobjet.backend.domain.user.repository.ShopRepository;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.ErrorCode;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShopMapService {

    private final ShopRepository shopRepository;
    private final ContractRepository contractRepository;
    private final FavoriteRepository favoriteRepository;
    private final KakaoLocalClient kakaoLocalClient;
    private final ShopService shopService;

    public ShopMapResponse getShopMarkers() {
        List<ShopMapItemResponse> markers = loadShopMarkers();
        log.info("Loaded shop markers count={}", markers.size());
        return new ShopMapResponse(markers);
    }

    public ShopDetailResponse getShopDetail(Long currentUserId, Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND));
        boolean favorite = currentUserId != null
                && favoriteRepository.existsByUser_IdAndTargetTypeAndTargetId(
                        currentUserId,
                        FavoriteTargetType.SHOP,
                        shopId
                );

        return ShopDetailResponse.from(shop, shopService.getBusinessHours(shopId), favorite);
    }

    public ShopContractedArtistListResponse getContractedArtists(Long shopId) {
        if (!shopRepository.existsById(shopId)) {
            throw new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND);
        }

        return ShopContractedArtistListResponse.from(
                contractRepository.findCurrentContractedArtistRowsByShopId(
                        shopId,
                        ContractStatus.APPROVED,
                        LocalDate.now()
                )
        );
    }

    @Transactional
    public ShopGeocodeResponse geocodeShops() {
        List<Shop> shops = shopRepository.findAllByBusinessAddressIsNotNullAndBusinessAddressNot("");

        int updatedCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        for (Shop shop : shops) {
            if (shop.getLatitude() != null && shop.getLongitude() != null) {
                skippedCount++;
                continue;
            }

            try {
                ShopCoordinate coordinate = kakaoLocalClient.searchAddress(shop.getBusinessAddress());
                if (coordinate == null) {
                    failedCount++;
                    continue;
                }

                shop.updateCoordinates(coordinate.latitude(), coordinate.longitude());
                updatedCount++;
            } catch (RuntimeException exception) {
                log.error("Failed to geocode shopId={}, address={}", shop.getShopId(), shop.getBusinessAddress(), exception);
                failedCount++;
            }
        }

        return new ShopGeocodeResponse(shops.size(), updatedCount, skippedCount, failedCount);
    }

    private List<ShopMapItemResponse> loadShopMarkers() {
        List<Shop> shops = shopRepository.findAllByLatitudeIsNotNullAndLongitudeIsNotNull();
        log.info("Fetched shops with coordinates count={}", shops.size());

        return shops
                .stream()
                .peek(shop -> log.info(
                        "Preparing shop for map shopId={}, lat={}, lng={}",
                        shop.getShopId(),
                        shop.getLatitude(),
                        shop.getLongitude()
                ))
                .map(this::toShopMapItemResponse)
                .toList();
    }

    private ShopMapItemResponse toShopMapItemResponse(Shop shop) {
        return ShopMapItemResponse.builder()
                .shopId(shop.getShopId())
                .shopName(shop.getShopName())
                .businessName(shop.getBusinessName())
                .businessAddress(shop.getBusinessAddress())
                .latitude(shop.getLatitude())
                .longitude(shop.getLongitude())
                .build();
    }
}
