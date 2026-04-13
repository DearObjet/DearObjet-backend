package app.dearobjet.backend.domain.classes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClassesRepository extends JpaRepository<Classes, Long> {

    @EntityGraph(attributePaths = {"shop", "shop.user"})
    Page<Classes> findByShop_User_Id(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"shop", "shop.user"})
    Page<Classes> findByShop_ShopId(Long shopId, Pageable pageable);

    @EntityGraph(attributePaths = {"shop", "shop.user"})
    Optional<Classes> findByClassesIdAndShop_User_Id(Long classesId, Long userId);
}
