package app.dearobjet.backend.domain.favorite.service;

import app.dearobjet.backend.domain.favorite.dto.FavoriteShopItemResponse;
import app.dearobjet.backend.domain.favorite.dto.FavoriteShopListResponse;
import app.dearobjet.backend.domain.favorite.entity.Favorite;
import app.dearobjet.backend.domain.favorite.enums.FavoriteTargetType;
import app.dearobjet.backend.domain.favorite.repository.FavoriteRepository;
import app.dearobjet.backend.domain.shop.entity.Shop;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.repository.ShopRepository;
import app.dearobjet.backend.domain.user.repository.UserRepository;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.ErrorCode;
import app.dearobjet.backend.global.exception.UnauthorizedException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteService {

    private static final FavoriteTargetType SHOP_TARGET_TYPE = FavoriteTargetType.SHOP;

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    public FavoriteShopItemResponse addShopFavorite(Long userId, Long shopId) {
        User user = getCustomer(userId);
        Shop shop = getShop(shopId);

        Favorite favorite = favoriteRepository
                .findByUser_IdAndTargetTypeAndTargetId(userId, SHOP_TARGET_TYPE, shopId)
                .orElseGet(() -> favoriteRepository.save(
                        Favorite.builder()
                                .user(user)
                                .targetType(SHOP_TARGET_TYPE)
                                .targetId(shopId)
                                .lastModifiedAt(LocalDateTime.now())
                                .build()
                ));

        favorite.touch();

        return FavoriteShopItemResponse.from(favorite, shop);
    }

    public void removeShopFavorite(Long userId, Long shopId) {
        getCustomer(userId);
        getShop(shopId);

        favoriteRepository.deleteByUser_IdAndTargetTypeAndTargetId(userId, SHOP_TARGET_TYPE, shopId);
    }

    @Transactional(readOnly = true)
    public FavoriteShopListResponse getFavoriteShops(Long userId, int page, int size) {
        getCustomer(userId);

        int normalizedPage = Math.max(page, 1);
        int normalizedSize = Math.max(size, 1);

        PageRequest pageRequest = PageRequest.of(normalizedPage - 1, normalizedSize);
        Page<FavoriteShopItemResponse> favoritePage = favoriteRepository.findFavoriteShopItems(
                userId,
                SHOP_TARGET_TYPE,
                pageRequest
        );

        return FavoriteShopListResponse.from(favoritePage, normalizedPage);
    }

    private User getCustomer(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != Role.CUSTOMER) {
            throw new UnauthorizedException(ErrorCode.FORBIDDEN, "일반 고객만 찜 기능을 사용할 수 있습니다.");
        }

        return user;
    }

    private Shop getShop(Long shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "샵을 찾을 수 없습니다."));
    }
}
