package app.dearobjet.backend.domain.contract.repository;

import app.dearobjet.backend.domain.contract.dto.projection.ContractInventoryProductRow;
import app.dearobjet.backend.domain.contract.entity.ContractProduct;
import app.dearobjet.backend.domain.contract.enums.ContractProductListingStatus;
import app.dearobjet.backend.domain.contract.enums.ContractProductStockMovementType;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContractProductRepository extends JpaRepository<ContractProduct, Long> {

    @Query("""
            select cp.contractProductsId as contractProductId,
                   coalesce(sum(m.quantityDelta), 0) as totalQuantity,
                   p.productUrl as productImageUrl,
                   p.productName as productName,
                   cp.sellingPrice as sellingPrice,
                   cp.stockQuantity as stockQuantity,
                   sac.commissionType as commissionType,
                   sac.commissionValue as commissionValue,
                   cp.marginAmount as marginAmount,
                   cp.unitSettlementAmount as unitSettlementAmount,
                   bp.businessName as artistName,
                   cp.recentStockedAt as recentStockedAt
            from ContractProduct cp
            join cp.shopArtistContract sac
            join sac.artist a
            join a.businessProfile bp
            join cp.product p
            left join ContractProductStockMovement m
                on m.contractProduct = cp
               and m.movementType = :inboundType
            where sac.shopArtistContractsId = :contractId
              and sac.shop.shopId = :shopId
              and sac.contractStatus = :contractStatus
              and cp.listingStatus = :listingStatus
            group by cp.contractProductsId,
                     p.productUrl,
                     p.productName,
                     cp.sellingPrice,
                     cp.stockQuantity,
                     sac.commissionType,
                     sac.commissionValue,
                     cp.marginAmount,
                     cp.unitSettlementAmount,
                     bp.businessName,
                     cp.recentStockedAt
            order by cp.recentStockedAt desc, cp.contractProductsId desc
            """)
    List<ContractInventoryProductRow> findInventoryProductRowsByContractId(
            @Param("contractId") Long contractId,
            @Param("shopId") Long shopId,
            @Param("contractStatus") ContractStatus contractStatus,
            @Param("listingStatus") ContractProductListingStatus listingStatus,
            @Param("inboundType") ContractProductStockMovementType inboundType
    );

    @Query("""
            select cp
            from ContractProduct cp
            join fetch cp.shopArtistContract sac
            where cp.contractProductsId = :contractProductId
              and sac.shop.shopId = :shopId
              and sac.contractStatus = :contractStatus
              and cp.listingStatus = :listingStatus
            """)
    Optional<ContractProduct> findOwnedInventoryById(
            @Param("contractProductId") Long contractProductId,
            @Param("shopId") Long shopId,
            @Param("contractStatus") ContractStatus contractStatus,
            @Param("listingStatus") ContractProductListingStatus listingStatus
    );
}
