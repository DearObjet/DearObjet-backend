package app.dearobjet.backend.domain.shop.repository;

import app.dearobjet.backend.domain.shop.entity.Product;
import app.dearobjet.backend.domain.shop.enums.ProductStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByArtistIdAndStatus(Long artistId, ProductStatus status, Pageable pageable);

    Optional<Product> findByProductsIdAndArtistIdAndStatus(
            Long productsId,
            Long artistId,
            ProductStatus status
    );

    List<Product> findByProductsIdInAndArtistIdAndStatus(
            Collection<Long> productsIds,
            Long artistId,
            ProductStatus status
    );
}
