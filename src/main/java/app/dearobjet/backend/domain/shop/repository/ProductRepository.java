package app.dearobjet.backend.domain.shop.repository;

import app.dearobjet.backend.domain.shop.entity.Product;
import app.dearobjet.backend.domain.shop.enums.ProductStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
            select p
            from Product p
            where p.artist.id = :artistId
              and p.status = :status
              and p.stockQuantity > 0
              and (
                  :keyword = ''
                  or lower(p.productName) like concat('%', :keyword, '%')
              )
            """)
    Page<Product> findShipmentAvailableProducts(
            @Param("artistId") Long artistId,
            @Param("status") ProductStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
