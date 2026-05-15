package app.dearobjet.backend.domain.contract;

import app.dearobjet.backend.domain.contract.dto.projection.ArtistAccountSearchRow;
import app.dearobjet.backend.domain.contract.dto.projection.ArtistSuggestionRow;
import app.dearobjet.backend.domain.contract.dto.projection.ArtistShipmentShopRow;
import app.dearobjet.backend.domain.contract.dto.projection.CompletedContractDocumentRow;
import app.dearobjet.backend.domain.contract.dto.projection.ContractInventoryRow;
import app.dearobjet.backend.domain.contract.dto.projection.InProgressContractDocumentRow;
import app.dearobjet.backend.domain.contract.dto.projection.ManagedArtistContractRow;
import app.dearobjet.backend.domain.contract.dto.projection.ManagedShopContractRow;
import app.dearobjet.backend.domain.contract.dto.projection.ShopContractedArtistRow;
import app.dearobjet.backend.domain.contract.dto.projection.ShopSuggestionRow;
import app.dearobjet.backend.domain.contract.entity.ShopArtistContract;
import app.dearobjet.backend.domain.contract.enums.ContractDocumentStatus;
import app.dearobjet.backend.domain.contract.enums.ContractProductListingStatus;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.enums.UserStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ContractRepository extends JpaRepository<ShopArtistContract, Long> {

    long countByShopUserIdAndContractStatus(Long userId, ContractStatus contractStatus);

    @Query("""
            select c.shopArtistContractsId as contractId,
                   a.id as artistId,
                   coalesce(nullif(c.artistContractName, ''), bp.businessName) as artistName,
                   c.artistContractBusinessNumber as artistBusinessNumber,
                   c.artistContractContact as artistContact,
                   c.contractDate as contractDate,
                   c.contractStartDate as contractStartDate,
                   c.contractEndDate as contractEndDate
            from ShopArtistContract c
            join c.artist a
            join a.businessProfile bp
            where c.shop.shopId = :shopId
              and c.contractStatus = :contractStatus
              and c.contractDocumentStatus = :documentStatus
              and c.shopDocumentDeletedAt is null
            order by case when c.contractDate is null then 1 else 0 end,
                     c.contractDate desc,
                     c.shopArtistContractsId desc
            """)
    List<CompletedContractDocumentRow> findCompletedDocumentRowsByShopId(
            @Param("shopId") Long shopId,
            @Param("contractStatus") ContractStatus contractStatus,
            @Param("documentStatus") ContractDocumentStatus documentStatus
    );

    @Query("""
            select c.shopArtistContractsId as contractId,
                   a.id as counterpartyId,
                   coalesce(nullif(c.artistContractName, ''), bp.businessName) as counterpartyName,
                   c.contractDate as contractDate,
                   c.contractStartDate as contractStartDate,
                   c.contractEndDate as contractEndDate,
                   c.contractDocumentStatus as contractDocumentStatus
            from ShopArtistContract c
            join c.artist a
            join a.businessProfile bp
            where c.shop.shopId = :shopId
              and c.contractStatus = :contractStatus
              and c.contractDocumentStatus in :documentStatuses
            order by c.shopArtistContractsId desc
            """)
    List<InProgressContractDocumentRow> findInProgressDocumentRowsByShopId(
            @Param("shopId") Long shopId,
            @Param("contractStatus") ContractStatus contractStatus,
            @Param("documentStatuses") List<ContractDocumentStatus> documentStatuses
    );

    @Query("""
            select c.shopArtistContractsId as contractId,
                   s.shopId as counterpartyId,
                   bp.businessName as counterpartyName,
                   c.contractDate as contractDate,
                   c.contractStartDate as contractStartDate,
                   c.contractEndDate as contractEndDate,
                   c.contractDocumentStatus as contractDocumentStatus
            from ShopArtistContract c
            join c.shop s
            join s.businessProfile bp
            where c.artist.id = :artistId
              and c.contractStatus = :contractStatus
              and c.contractDocumentStatus in :documentStatuses
            order by c.shopArtistContractsId desc
            """)
    List<InProgressContractDocumentRow> findInProgressDocumentRowsByArtistId(
            @Param("artistId") Long artistId,
            @Param("contractStatus") ContractStatus contractStatus,
            @Param("documentStatuses") List<ContractDocumentStatus> documentStatuses
    );

    @Query("""
            select c
            from ShopArtistContract c
            join fetch c.shop s
            where c.shopArtistContractsId in :contractIds
              and s.shopId = :shopId
            """)
    List<ShopArtistContract> findAllByIdsAndShopId(
            @Param("contractIds") List<Long> contractIds,
            @Param("shopId") Long shopId
    );

    @Query("""
            select count(c) > 0
            from ShopArtistContract c
            where c.shop.shopId = :shopId
              and c.artist.id = :artistId
              and (
                  c.contractStatus = :pendingStatus
                  or (
                      c.contractStatus = :approvedStatus
                      and (
                          c.contractEndDate is null
                          or c.contractEndDate >= :today
                      )
                  )
              )
            """)
    boolean existsPendingOrCurrentApprovedContract(
            @Param("shopId") Long shopId,
            @Param("artistId") Long artistId,
            @Param("pendingStatus") ContractStatus pendingStatus,
            @Param("approvedStatus") ContractStatus approvedStatus,
            @Param("today") LocalDate today
    );

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
            select a.id as artistId,
                   bp.businessName as artistName,
                   u.profileUrl as artistImageUrl
            from ShopArtistContract c
            join c.artist a
            join a.user u
            join a.businessProfile bp
            where c.shop.shopId = :shopId
              and c.contractStatus = :contractStatus
              and (
                  c.contractEndDate is null
                  or c.contractEndDate >= :today
              )
              and not exists (
                  select 1
                  from ShopArtistContract c2
                  where c2.shop = c.shop
                    and c2.artist = c.artist
                    and c2.contractStatus = :contractStatus
                    and (
                        c2.contractEndDate is null
                        or c2.contractEndDate >= :today
                    )
                    and c2.shopArtistContractsId > c.shopArtistContractsId
              )
            order by bp.businessName asc,
                     a.id desc
            """)
    List<ShopContractedArtistRow> findCurrentContractedArtistRowsByShopId(
            @Param("shopId") Long shopId,
            @Param("contractStatus") ContractStatus contractStatus,
            @Param("today") LocalDate today
    );

    @Query("""
            select c.shopArtistContractsId as contractId,
                   s.shopId as shopId,
                   bp.businessName as shopName,
                   bp.specialty as specialty,
                   c.contractStartDate as contractStartDate,
                   c.contractEndDate as contractEndDate,
                   max(cp.recentStockedAt) as recentStockedAt,
                   c.recentInboundConfirmed as inboundConfirmed
            from ShopArtistContract c
            join c.shop s
            join s.businessProfile bp
            left join ContractProduct cp
                on cp.shopArtistContract = c
               and cp.listingStatus = :listingStatus
            where c.artist.id = :artistId
              and c.contractStatus = :contractStatus
              and (
                  c.contractEndDate is null
                  or c.contractEndDate >= :today
              )
              and not exists (
                  select 1
                  from ShopArtistContract c2
                  where c2.artist = c.artist
                    and c2.shop = c.shop
                    and c2.contractStatus = :contractStatus
                    and (
                        c2.contractEndDate is null
                        or c2.contractEndDate >= :today
                    )
                    and c2.shopArtistContractsId > c.shopArtistContractsId
              )
            group by c.shopArtistContractsId,
                     s.shopId,
                     bp.businessName,
                     bp.specialty,
                     c.contractStartDate,
                     c.contractEndDate,
                     c.recentInboundConfirmed
            order by case when max(cp.recentStockedAt) is null then 1 else 0 end,
                     max(cp.recentStockedAt) desc,
                     c.shopArtistContractsId desc
            """)
    List<ArtistShipmentShopRow> findShipmentShopRowsByArtistId(
            @Param("artistId") Long artistId,
            @Param("contractStatus") ContractStatus contractStatus,
            @Param("listingStatus") ContractProductListingStatus listingStatus,
            @Param("today") java.time.LocalDate today
    );

    @Query("""
            select c
            from ShopArtistContract c
            join fetch c.shop s
            join fetch s.businessProfile
            where c.artist.id = :artistId
              and s.shopId = :shopId
              and c.contractStatus = :contractStatus
            order by c.shopArtistContractsId desc
            """)
    List<ShopArtistContract> findApprovedContractsByArtistIdAndShopId(
            @Param("artistId") Long artistId,
            @Param("shopId") Long shopId,
            @Param("contractStatus") ContractStatus contractStatus
    );

    @Query("""
            select c
            from ShopArtistContract c
            join fetch c.shop s
            join fetch s.businessProfile
            where c.artist.id = :artistId
              and s.shopId = :shopId
              and c.contractStatus = :contractStatus
              and (
                  c.contractEndDate is null
                  or c.contractEndDate >= :today
              )
            order by c.shopArtistContractsId desc
            """)
    List<ShopArtistContract> findCurrentApprovedContractsByArtistIdAndShopId(
            @Param("artistId") Long artistId,
            @Param("shopId") Long shopId,
            @Param("contractStatus") ContractStatus contractStatus,
            @Param("today") java.time.LocalDate today
    );

    @Query("""
            select c
            from ShopArtistContract c
            where c.artist.id = :artistId
              and c.shop.shopId = :shopId
              and c.contractStatus in :contractStatuses
            order by c.shopArtistContractsId desc
            """)
    List<ShopArtistContract> findShipmentProductContractsByArtistIdAndShopId(
            @Param("artistId") Long artistId,
            @Param("shopId") Long shopId,
            @Param("contractStatuses") List<ContractStatus> contractStatuses
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
                   c.contractRequestType as contractRequestType,
                   c.contractDocumentStatus as contractDocumentStatus
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

    @Query("""
            select c.shopArtistContractsId as contractId,
                   s.shopId as shopId,
                   bp.businessName as shopName,
                   c.contractStartDate as contractStartDate,
                   c.contractEndDate as contractEndDate,
                   c.contractStatus as contractStatus,
                   c.contractRequestType as contractRequestType,
                   c.terminatedAt as terminatedAt
            from ShopArtistContract c
            join c.shop s
            join s.businessProfile bp
            where c.artist.id = :artistId
              and (
                  c.contractStatus in :contractStatuses
                  or (
                      c.contractStatus = :terminatedStatus
                      and (c.terminatedAt is null or c.terminatedAt > :terminatedVisibleAfter)
                  )
              )
            order by case
                        when c.contractStatus = :pendingStatus then 0
                        when c.contractStatus = :approvedStatus then 1
                        when c.contractStatus = :endedStatus then 2
                        when c.contractStatus = :terminatedStatus then 3
                        else 4
                     end,
                     c.shopArtistContractsId desc
            """)
    List<ManagedShopContractRow> findManagedShopRowsByArtistId(
            @Param("artistId") Long artistId,
            @Param("contractStatuses") List<ContractStatus> contractStatuses,
            @Param("terminatedStatus") ContractStatus terminatedStatus,
            @Param("terminatedVisibleAfter") LocalDateTime terminatedVisibleAfter,
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
              and c.artist.id = :artistId
              and (
                  c.contractStatus in :contractStatuses
                  or (
                      c.contractStatus = :terminatedStatus
                      and (c.terminatedAt is null or c.terminatedAt > :terminatedVisibleAfter)
                  )
              )
            """)
    Optional<ShopArtistContract> findManagedShopContractByIdAndArtistId(
            @Param("contractId") Long contractId,
            @Param("artistId") Long artistId,
            @Param("contractStatuses") List<ContractStatus> contractStatuses,
            @Param("terminatedStatus") ContractStatus terminatedStatus,
            @Param("terminatedVisibleAfter") LocalDateTime terminatedVisibleAfter
    );

    @Query("""
            select c
            from ShopArtistContract c
            join fetch c.artist a
            join fetch a.businessProfile
            join fetch c.shop s
            join fetch s.businessProfile
            where c.shopArtistContractsId = :contractId
              and c.artist.id = :artistId
            """)
    Optional<ShopArtistContract> findContractByIdAndArtistId(
            @Param("contractId") Long contractId,
            @Param("artistId") Long artistId
    );

    @Query("""
            select a.id as artistId,
                   u.id as userId,
                   u.name as name,
                   bp.businessName as artistName,
                   u.profileUrl as artistImageUrl,
                   bp.specialty as specialty,
                   a.instagramId as instagramId
            from Artist a
            join a.user u
            join a.businessProfile bp
            where u.role = :artistRole
              and u.userStatus = :activeStatus
              and not exists (
                  select 1
                  from ShopArtistContract c
                  where c.shop.shopId = :shopId
                    and c.artist = a
                    and (
                        c.contractStatus = :pendingStatus
                        or (
                            c.contractStatus = :approvedStatus
                            and (
                                c.contractEndDate is null
                                or c.contractEndDate >= :today
                            )
                        )
                    )
              )
            """)
    List<ArtistSuggestionRow> findArtistSuggestionRows(
            @Param("shopId") Long shopId,
            @Param("artistRole") Role artistRole,
            @Param("activeStatus") UserStatus activeStatus,
            @Param("pendingStatus") ContractStatus pendingStatus,
            @Param("approvedStatus") ContractStatus approvedStatus,
            @Param("today") java.time.LocalDate today
    );

    @Query("""
            select s.shopId as shopId,
                   u.id as userId,
                   bp.businessName as shopName,
                   u.profileUrl as shopImageUrl,
                   bp.specialty as specialty,
                   s.instagramId as instagramId
            from Shop s
            join s.user u
            join s.businessProfile bp
            where u.role = :shopRole
              and u.userStatus = :activeStatus
              and not exists (
                  select 1
                  from ShopArtistContract c
                  where c.artist.id = :artistId
                    and c.shop = s
                    and (
                        c.contractStatus = :pendingStatus
                        or (
                            c.contractStatus = :approvedStatus
                            and (
                                c.contractEndDate is null
                                or c.contractEndDate >= :today
                            )
                        )
                    )
              )
            """)
    List<ShopSuggestionRow> findShopSuggestionRows(
            @Param("artistId") Long artistId,
            @Param("shopRole") Role shopRole,
            @Param("activeStatus") UserStatus activeStatus,
            @Param("pendingStatus") ContractStatus pendingStatus,
            @Param("approvedStatus") ContractStatus approvedStatus,
            @Param("today") java.time.LocalDate today
    );

    @Query("""
            select a.id as artistId,
                   u.id as userId,
                   u.name as name,
                   bp.businessName as artistName,
                   u.profileUrl as artistImageUrl,
                   bp.specialty as specialty,
                   a.instagramId as instagramId,
                   u.email as email
            from Artist a
            join a.user u
            join a.businessProfile bp
            where u.role = :artistRole
              and u.userStatus = :activeStatus
              and (
                  lower(bp.businessName) like :keyword
                  or lower(u.name) like :keyword
                  or lower(u.email) like :keyword
              )
              and not exists (
                  select 1
                  from ShopArtistContract c
                  where c.shop.shopId = :shopId
                    and c.artist = a
                    and (
                        c.contractStatus = :pendingStatus
                        or (
                            c.contractStatus = :approvedStatus
                            and (
                                c.contractEndDate is null
                                or c.contractEndDate >= :today
                            )
                        )
                    )
              )
            order by bp.businessName asc, a.id desc
            """)
    List<ArtistAccountSearchRow> searchArtistAccountRows(
            @Param("shopId") Long shopId,
            @Param("artistRole") Role artistRole,
            @Param("activeStatus") UserStatus activeStatus,
            @Param("pendingStatus") ContractStatus pendingStatus,
            @Param("approvedStatus") ContractStatus approvedStatus,
            @Param("today") java.time.LocalDate today,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
