package app.dearobjet.backend.domain.shop.repository;

import app.dearobjet.backend.domain.shop.entity.Shop;
import app.dearobjet.backend.domain.shop.entity.ShopBusinessHour;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopBusinessHourRepository extends JpaRepository<ShopBusinessHour, Long> {

    List<ShopBusinessHour> findAllByShop(Shop shop);

    Optional<ShopBusinessHour> findByShopAndDayOfWeek(Shop shop, java.time.DayOfWeek dayOfWeek);
}
