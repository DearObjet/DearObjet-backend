package app.dearobjet.backend.domain.favorite.repository;

import app.dearobjet.backend.domain.favorite.dto.FavoriteShopItemResponse;
import app.dearobjet.backend.domain.favorite.entity.Favorite;
import app.dearobjet.backend.domain.favorite.enums.FavoriteTargetType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUser_IdAndTargetTypeAndTargetId(
            Long userId,
            FavoriteTargetType targetType,
            Long targetId
    );

    void deleteByUser_IdAndTargetTypeAndTargetId(
            Long userId,
            FavoriteTargetType targetType,
            Long targetId
    );

    @Query(
            value = """
                    select new app.dearobjet.backend.domain.favorite.dto.FavoriteShopItemResponse(
                        f.favoriteId,
                        s.shopId,
                        bp.businessName,
                        u.profileUrl,
                        bp.businessAddress,
                        u.phoneNumber,
                        s.instagramId,
                        s.latitude,
                        s.longitude,
                        f.createdAt
                    )
                    from Favorite f
                    join Shop s on s.shopId = f.targetId
                    join s.businessProfile bp
                    join s.user u
                    where f.user.id = :userId
                      and f.targetType = :targetType
                    order by f.createdAt desc, f.favoriteId desc
                    """,
            countQuery = """
                    select count(f)
                    from Favorite f
                    join Shop s on s.shopId = f.targetId
                    where f.user.id = :userId
                      and f.targetType = :targetType
                    """
    )
    Page<FavoriteShopItemResponse> findFavoriteShopItems(
            @Param("userId") Long userId,
            @Param("targetType") FavoriteTargetType targetType,
            Pageable pageable
    );
}
