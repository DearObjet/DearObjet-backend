package app.dearobjet.backend.domain.contract;

import app.dearobjet.backend.domain.contract.dto.projection.ContractInventoryRow;
import app.dearobjet.backend.domain.contract.dto.projection.ManagedArtistContractRow;
import app.dearobjet.backend.domain.contract.entity.ShopArtistContract;
import app.dearobjet.backend.domain.contract.enums.ContractProductListingStatus;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContractRepository extends JpaRepository<ShopArtistContract, Long> {

    long countByShopUserIdAndContractStatus(Long userId, ContractStatus contractStatus);

    @Query("""
            select coalesce(nullif(trim(bp.businessName), ''), nullif(trim(u.name), ''), 'UNKNOWN')
            from ShopArtistContract c
            join c.artist a
            join a.businessProfile bp
            join a.user u
            where c.shop.user.id = :userId
              and c.contractStatus = :contractStatus
            order by c.shopArtistContractsId desc
            """)
    List<String> findPendingApplicationArtistNamesByShopUserId(
            @Param("userId") Long userId,
            @Param("contractStatus") ContractStatus contractStatus
    );

    @Query("""
            select c.shopArtistContractsId as contractId,
                   u.profileUrl as artistImageUrl,
                   bp.businessName as artistName,
                   bp.specialty as specialty,
                   max(cp.recentStockedAt) as recentStockedAt,
                   c.recentInboundConfirmed as inboundConfirmed
            from ShopArtistContract c
            join c.artist a
            join a.user u
            join a.businessProfile bp
            left join ContractProduct cp
                on cp.shopArtistContract = c
               and cp.listingStatus = :listingStatus
            where c.shop.shopId = :shopId
              and c.contractStatus = :contractStatus
            group by c.shopArtistContractsId,
                     u.profileUrl,
                     bp.businessName,
                     bp.specialty,
                     c.recentInboundConfirmed
            order by case when max(cp.recentStockedAt) is null then 1 else 0 end,
                     max(cp.recentStockedAt) desc,
                     c.shopArtistContractsId desc
            """)
    List<ContractInventoryRow> findInventoryRowsByShopId(
            @Param("shopId") Long shopId,
            @Param("contractStatus") ContractStatus contractStatus,
            @Param("listingStatus") ContractProductListingStatus listingStatus
    );

    @Query("""
            select c
            from ShopArtistContract c
            join fetch c.artist a
            join fetch a.businessProfile
            where c.shopArtistContractsId = :contractId
              and c.shop.shopId = :shopId
              and c.contractStatus = :contractStatus
            """)
    Optional<ShopArtistContract> findApprovedByIdAndShopId(
            @Param("contractId") Long contractId,
            @Param("shopId") Long shopId,
            @Param("contractStatus") ContractStatus contractStatus
    );

    @Query("""
            select c.shopArtistContractsId as contractId,
                   a.id as artistId,
                   bp.businessName as artistName,
                   c.contractStartDate as contractStartDate,
                   c.contractEndDate as contractEndDate,
                   c.contractStatus as contractStatus,
                   c.contractRequestType as contractRequestType
            from ShopArtistContract c
            join c.artist a
            join a.businessProfile bp
            where c.shop.shopId = :shopId
              and c.contractStatus in :contractStatuses
            order by case
                        when c.contractStatus = :pendingStatus then 0
                        when c.contractStatus = :approvedStatus then 1
                        when c.contractStatus = :endedStatus then 2
                        else 3
                     end,
                     c.shopArtistContractsId desc
            """)
    List<ManagedArtistContractRow> findManagedArtistRowsByShopId(
            @Param("shopId") Long shopId,
            @Param("contractStatuses") List<ContractStatus> contractStatuses,
            @Param("pendingStatus") ContractStatus pendingStatus,
            @Param("approvedStatus") ContractStatus approvedStatus,
            @Param("endedStatus") ContractStatus endedStatus
    );

    @Query("""
            select c
            from ShopArtistContract c
            join fetch c.artist a
            join fetch a.businessProfile
            join fetch c.shop s
            join fetch s.businessProfile
            where c.shopArtistContractsId = :contractId
              and c.shop.shopId = :shopId
              and c.contractStatus in :contractStatuses
            """)
    Optional<ShopArtistContract> findManagedArtistContractByIdAndShopId(
            @Param("contractId") Long contractId,
            @Param("shopId") Long shopId,
            @Param("contractStatuses") List<ContractStatus> contractStatuses
    );
}
