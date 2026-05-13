package app.dearobjet.backend.domain.shop.repository;

import app.dearobjet.backend.domain.shop.entity.ShopReview;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopReviewRepository extends JpaRepository<ShopReview, Long> {

    @EntityGraph(attributePaths = {"user"})
    List<ShopReview> findByShop_ShopId(Long shopId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    List<ShopReview> findByShop_ShopIdAndShopReviewIdLessThan(Long shopId, Long shopReviewId, Pageable pageable);

    @EntityGraph(attributePaths = {"shop", "user"})
    Optional<ShopReview> findByShopReviewIdAndShop_ShopId(Long shopReviewId, Long shopId);
}
