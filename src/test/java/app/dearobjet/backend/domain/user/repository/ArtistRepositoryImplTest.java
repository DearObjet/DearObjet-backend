package app.dearobjet.backend.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import app.dearobjet.backend.domain.artist.entity.Artist;
import app.dearobjet.backend.domain.artist.support.ArtistRandomOrder;
import app.dearobjet.backend.domain.user.entity.BusinessProfile;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.enums.BusinessCategory;
import app.dearobjet.backend.domain.user.enums.BusinessType;
import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.enums.Specialty;
import app.dearobjet.backend.domain.user.enums.UserStatus;
import app.dearobjet.backend.global.common.config.JpaConfig;
import jakarta.persistence.EntityManager;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
class ArtistRepositoryImplTest {

    private static final BusinessType DEFAULT_BUSINESS_TYPE = BusinessType.WHOLESALE_RETAIL;
    private static final BusinessCategory DEFAULT_BUSINESS_CATEGORY = BusinessCategory.CRAFT_RETAIL;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private BusinessProfileRepository businessProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("seed 기반 정렬 키와 커서 페이징을 일관되게 반환한다")
    void givenSeedAndCursor_whenFindRandomArtists_thenReturnDeterministicPages() {
        List<Artist> activeArtists = IntStream.rangeClosed(1, 5)
                .mapToObj(index -> createArtist(index, UserStatus.ACTIVE))
                .toList();
        Artist inactiveArtist = createArtist(99, UserStatus.INACTIVE);
        flushAndClear();

        long seed = 123_456L;
        List<Long> expectedOrder = activeArtists.stream()
                .map(Artist::getId)
                .sorted(Comparator
                        .comparingLong((Long artistId) -> ArtistRandomOrder.calculateOrderKey(seed, artistId))
                        .thenComparingLong(Long::longValue))
                .toList();

        List<Artist> firstPage = artistRepository.findRandomArtists(seed, null, 3);
        List<Artist> secondPage = artistRepository.findRandomArtists(seed, firstPage.get(2).getId(), 3);

        assertThat(firstPage).extracting(Artist::getId)
                .containsExactlyElementsOf(expectedOrder.subList(0, 3));
        assertThat(secondPage).extracting(Artist::getId)
                .containsExactlyElementsOf(expectedOrder.subList(3, expectedOrder.size()));
        assertThat(firstPage).extracting(Artist::getId).doesNotContain(inactiveArtist.getId());
        assertThat(secondPage).extracting(Artist::getId).doesNotContain(inactiveArtist.getId());
    }

    @Test
    @DisplayName("seed가 바뀌면 작가 노출 순서가 달라진다")
    void givenDifferentSeeds_whenFindRandomArtists_thenReturnDifferentOrders() {
        IntStream.rangeClosed(1, 8)
                .forEach(index -> createArtist(index, UserStatus.ACTIVE));
        flushAndClear();

        List<Long> firstOrder = artistRepository.findRandomArtists(0L, null, 8).stream()
                .map(Artist::getId)
                .toList();
        List<Long> secondOrder = artistRepository.findRandomArtists(1L, null, 8).stream()
                .map(Artist::getId)
                .toList();

        assertThat(firstOrder).hasSize(8);
        assertThat(secondOrder).hasSize(8);
        assertThat(firstOrder).isNotEqualTo(secondOrder);
    }

    private Artist createArtist(int index, UserStatus userStatus) {
        User user = userRepository.save(User.builder()
                .email("artist" + index + "@test.com")
                .name("Artist " + index)
                .phoneNumber("0101000" + String.format("%04d", index))
                .profileUrl("https://image.test/artist-" + index + ".png")
                .role(Role.ARTIST)
                .userStatus(userStatus)
                .build());

        BusinessProfile businessProfile = businessProfileRepository.save(BusinessProfile.builder()
                .user(user)
                .businessType(DEFAULT_BUSINESS_TYPE)
                .businessNumber("ART-" + index)
                .businessName("Artist Studio " + index)
                .ownerName(user.getName())
                .businessAddress("서울시 성수동 테스트로 " + index)
                .businessLicenseUrl("https://image.test/license-" + index + ".png")
                .businessCategory(DEFAULT_BUSINESS_CATEGORY)
                .specialty(Specialty.values()[(index - 1) % Specialty.values().length])
                .reviewDataAgreement(Boolean.TRUE)
                .build());

        return artistRepository.save(Artist.builder()
                .user(user)
                .businessProfile(businessProfile)
                .build());
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
