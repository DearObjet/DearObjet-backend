package app.dearobjet.backend.domain.contract;

import app.dearobjet.backend.domain.contract.entity.ShopArtistContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContractRepository extends JpaRepository<ShopArtistContract, Long> {

    @Query("""
            select count(c)
            from ShopArtistContract c
            where c.shop.user.id = :userId
              and c.contractStatus = 'PENDING'
            """)
    long countPendingApplicationsByShopUserId(@Param("userId") Long userId);

    @Query("""
            select coalesce(nullif(trim(a.businessName), ''), nullif(trim(u.name), ''), 'UNKNOWN')
            from ShopArtistContract c
            join c.artist a
            join a.user u
            where c.shop.user.id = :userId
              and c.contractStatus = 'PENDING'
            order by c.shopArtistContractsId desc
            """)
    List<String> findPendingApplicationArtistNamesByShopUserId(@Param("userId") Long userId);
}
