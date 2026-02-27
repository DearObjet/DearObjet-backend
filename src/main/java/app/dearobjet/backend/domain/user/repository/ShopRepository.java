package app.dearobjet.backend.domain.user.repository;

import app.dearobjet.backend.domain.shop.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopRepository extends JpaRepository<Shop, Long> {
}
