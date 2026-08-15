package app.dearobjet.backend.domain.classes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClassesRepository extends JpaRepository<Classes, Long> {

    @EntityGraph(attributePaths = {"shop", "shop.user"})
    Page<Classes> findByShop_User_Id(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"shop", "shop.user"})
    Page<Classes> findByShop_ShopIdAndBlindedFalse(Long shopId, Pageable pageable);

    @EntityGraph(attributePaths = {"shop", "shop.user"})
    Optional<Classes> findByIdAndShop_User_Id(Long classId, Long userId);

    @EntityGraph(attributePaths = {"shop", "shop.businessProfile"})
    @Query("select c from Classes c where "
            + "lower(c.className) like lower(concat('%', :keyword, '%')) "
            + "or lower(c.shop.businessProfile.businessName) like lower(concat('%', :keyword, '%'))")
    Page<Classes> searchForAdmin(@Param("keyword") String keyword, Pageable pageable);
}
