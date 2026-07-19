package app.dearobjet.backend.domain.user.repository;

import app.dearobjet.backend.domain.chat.dto.projection.ChatUserSearchRow;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.enums.UserStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findBySocialId(String socialId);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    long countByRole(Role role);

    @Query("""
            select u
            from User u
            where u.role <> 'ADMIN'
              and (:role is null or u.role = :role)
              and (
                    :keyword is null
                    or lower(u.name) like :keyword
                    or lower(u.email) like :keyword
              )
            """)
    Page<User> searchForAdmin(
            @Param("role") Role role,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query(
            value = """
                    select u.id as userId,
                           u.name as accountName,
                           u.role as role,
                           u.profileUrl as profileImageUrl,
                           bp.specialty as specialty,
                           case
                               when u.role = :shopRole
                                    and :currentArtistId is not null
                                    and exists (
                                        select 1
                                        from ShopArtistContract c
                                        where c.shop = s
                                          and c.artist.id = :currentArtistId
                                          and c.contractStatus = :pendingStatus
                                    )
                               then true
                               else false
                           end as pendingContractExists,
                           case
                               when u.role = :shopRole
                                    and :currentArtistId is not null
                                    and exists (
                                        select 1
                                        from ShopArtistContract c
                                        where c.shop = s
                                          and c.artist.id = :currentArtistId
                                          and c.contractStatus = :approvedStatus
                                          and (
                                              c.contractEndDate is null
                                              or c.contractEndDate >= :today
                                          )
                                    )
                               then true
                               else false
                           end as approvedContractExists
                    from User u
                    left join BusinessProfile bp on bp.user = u
                    left join Shop s on s.user = u
                    where u.userStatus = :activeStatus
                      and u.role in :searchableRoles
                      and u.id <> :currentUserId
                      and (:keyword is null or lower(u.name) like :keyword)
                    order by u.name asc, u.id desc
                    """,
            countQuery = """
                    select count(u)
                    from User u
                    where u.userStatus = :activeStatus
                      and u.role in :searchableRoles
                      and u.id <> :currentUserId
                      and (:keyword is null or lower(u.name) like :keyword)
                    """
    )
    Page<ChatUserSearchRow> searchChatUsers(
            @Param("currentUserId") Long currentUserId,
            @Param("keyword") String keyword,
            @Param("searchableRoles") List<Role> searchableRoles,
            @Param("shopRole") Role shopRole,
            @Param("activeStatus") UserStatus activeStatus,
            @Param("pendingStatus") ContractStatus pendingStatus,
            @Param("approvedStatus") ContractStatus approvedStatus,
            @Param("today") LocalDate today,
            @Param("currentArtistId") Long currentArtistId,
            Pageable pageable
    );
}
