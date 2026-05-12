package app.dearobjet.backend.domain.user.repository;

import app.dearobjet.backend.domain.artist.entity.Artist;
import app.dearobjet.backend.domain.chat.dto.projection.ChatUserSearchRow;
import app.dearobjet.backend.domain.contract.ContractRepository;
import app.dearobjet.backend.domain.contract.entity.ShopArtistContract;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import app.dearobjet.backend.domain.shop.entity.Shop;
import app.dearobjet.backend.domain.user.entity.BusinessProfile;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.enums.BusinessCategory;
import app.dearobjet.backend.domain.user.enums.BusinessType;
import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.enums.Specialty;
import app.dearobjet.backend.domain.user.enums.UserStatus;
import app.dearobjet.backend.global.common.config.JpaConfig;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
class UserRepositoryTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 5, 12);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private BusinessProfileRepository businessProfileRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Test
    @DisplayName("채팅 상대 검색은 활성 가입 완료 사용자만 계정명으로 조회한다")
    void givenUsers_whenSearchChatUsers_thenReturnActiveRegisteredUsersByAccountName() {
        // given
        User currentUser = userRepository.save(user("current@test.com", "현재 작가", Role.ARTIST, UserStatus.ACTIVE));
        BusinessProfile artistProfile = businessProfileRepository.save(businessProfile(currentUser, "현재 작가 스튜디오", Specialty.CERAMIC));
        Artist currentArtist = artistRepository.save(Artist.builder()
                .user(currentUser)
                .businessProfile(artistProfile)
                .build());

        User matchedCustomer = userRepository.save(user("customer@test.com", "검색 일반", Role.CUSTOMER, UserStatus.ACTIVE));
        User pendingShopUser = userRepository.save(user("pending-shop@test.com", "검색 대기샵", Role.SHOP, UserStatus.ACTIVE));
        Shop pendingShop = createShop(pendingShopUser, "검색 대기 소품샵", Specialty.LIVING_GOODS);
        User completedShopUser = userRepository.save(user("completed-shop@test.com", "검색 완료샵", Role.SHOP, UserStatus.ACTIVE));
        Shop completedShop = createShop(completedShopUser, "검색 완료 소품샵", Specialty.DESK_OFFICE);
        userRepository.save(user("temp@test.com", "검색 임시", Role.TEMP, UserStatus.ACTIVE));
        userRepository.save(user("inactive@test.com", "검색 비활성", Role.CUSTOMER, UserStatus.INACTIVE));

        contractRepository.save(ShopArtistContract.builder()
                .artist(currentArtist)
                .shop(pendingShop)
                .contractStatus(ContractStatus.PENDING)
                .build());
        contractRepository.save(ShopArtistContract.builder()
                .artist(currentArtist)
                .shop(completedShop)
                .contractStatus(ContractStatus.APPROVED)
                .contractEndDate(TODAY.plusDays(1))
                .build());

        // when
        Page<ChatUserSearchRow> rows = userRepository.searchChatUsers(
                currentUser.getId(),
                "%검색%",
                List.of(Role.CUSTOMER, Role.ARTIST, Role.SHOP),
                Role.SHOP,
                UserStatus.ACTIVE,
                ContractStatus.PENDING,
                ContractStatus.APPROVED,
                TODAY,
                currentArtist.getId(),
                PageRequest.of(0, 20)
        );

        // then
        assertThat(rows.getContent()).extracting(ChatUserSearchRow::getUserId)
                .containsExactlyInAnyOrder(matchedCustomer.getId(), pendingShopUser.getId(), completedShopUser.getId());
        assertThat(findByUserId(rows, pendingShopUser.getId()).getPendingContractExists()).isTrue();
        assertThat(findByUserId(rows, completedShopUser.getId()).getApprovedContractExists()).isTrue();
    }

    private ChatUserSearchRow findByUserId(Page<ChatUserSearchRow> rows, Long userId) {
        return rows.getContent().stream()
                .filter(row -> row.getUserId().equals(userId))
                .findFirst()
                .orElseThrow();
    }

    private Shop createShop(User user, String businessName, Specialty specialty) {
        BusinessProfile businessProfile = businessProfileRepository.save(businessProfile(user, businessName, specialty));
        return shopRepository.save(Shop.builder()
                .user(user)
                .businessProfile(businessProfile)
                .build());
    }

    private User user(String email, String name, Role role, UserStatus status) {
        return User.builder()
                .email(email)
                .name(name)
                .role(role)
                .userStatus(status)
                .build();
    }

    private BusinessProfile businessProfile(User user, String businessName, Specialty specialty) {
        return BusinessProfile.builder()
                .user(user)
                .businessType(BusinessType.WHOLESALE_RETAIL)
                .businessNumber("123-45-67890")
                .businessName(businessName)
                .ownerName("대표")
                .businessAddress("서울")
                .businessCategory(BusinessCategory.CRAFT_RETAIL)
                .specialty(specialty)
                .reviewDataAgreement(true)
                .build();
    }
}
