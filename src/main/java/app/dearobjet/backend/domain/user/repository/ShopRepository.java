package app.dearobjet.backend.domain.user.repository;

import app.dearobjet.backend.domain.shop.entity.Shop;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findByUser_Id(Long userId);

    @Query("""
            select s
            from Shop s
            join s.businessProfile bp
            where bp.businessAddress is not null
              and bp.businessAddress <> :businessAddress
            """)
    List<Shop> findAllByBusinessAddressIsNotNullAndBusinessAddressNot(String businessAddress);

    List<Shop> findAllByLatitudeIsNotNullAndLongitudeIsNotNull();
}
