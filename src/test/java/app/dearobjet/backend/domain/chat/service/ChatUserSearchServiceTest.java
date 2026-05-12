package app.dearobjet.backend.domain.chat.service;

import app.dearobjet.backend.domain.artist.entity.Artist;
import app.dearobjet.backend.domain.chat.dto.ChatUserSearchResponse;
import app.dearobjet.backend.domain.chat.dto.projection.ChatUserSearchRow;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.enums.Specialty;
import app.dearobjet.backend.domain.user.enums.UserStatus;
import app.dearobjet.backend.domain.user.repository.ArtistRepository;
import app.dearobjet.backend.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@DisplayName("ChatUserSearchService 테스트")
@ExtendWith(MockitoExtension.class)
class ChatUserSearchServiceTest {

    private static final Long CURRENT_USER_ID = 1L;
    private static final Long CURRENT_ARTIST_ID = 10L;
    private static final Long SHOP_USER_ID = 2L;
    private static final Long CUSTOMER_USER_ID = 3L;

    @InjectMocks
    private ChatUserSearchService chatUserSearchService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Test
    @DisplayName("검색어는 공백 제거 후 소문자 like 패턴으로 조회한다")
    void givenKeyword_whenSearch_thenNormalizeKeyword() {
        // given
        PageRequest pageable = PageRequest.of(0, 20);
        given(artistRepository.findByUserId(CURRENT_USER_ID)).willReturn(Optional.empty());
        given(userRepository.searchChatUsers(
                eq(CURRENT_USER_ID),
                any(),
                eq(List.of(Role.CUSTOMER, Role.ARTIST, Role.SHOP)),
                eq(Role.SHOP),
                eq(UserStatus.ACTIVE),
                eq(ContractStatus.PENDING),
                eq(ContractStatus.APPROVED),
                any(LocalDate.class),
                eq(null),
                eq(pageable)
        )).willReturn(Page.empty(pageable));

        // when
        chatUserSearchService.search(CURRENT_USER_ID, "  PostMan  ", pageable);

        // then
        ArgumentCaptor<String> keywordCaptor = ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(userRepository).searchChatUsers(
                eq(CURRENT_USER_ID),
                keywordCaptor.capture(),
                eq(List.of(Role.CUSTOMER, Role.ARTIST, Role.SHOP)),
                eq(Role.SHOP),
                eq(UserStatus.ACTIVE),
                eq(ContractStatus.PENDING),
                eq(ContractStatus.APPROVED),
                any(LocalDate.class),
                eq(null),
                eq(pageable)
        );
        assertThat(keywordCaptor.getValue()).isEqualTo("%postman%");
    }

    @Test
    @DisplayName("현재 사용자가 작가이고 검색 결과가 소품샵이면 계약 상태를 라벨로 변환한다")
    void givenArtistAndShopRows_whenSearch_thenReturnContractStatusLabels() {
        // given
        PageRequest pageable = PageRequest.of(0, 20);
        Artist artist = Artist.builder().id(CURRENT_ARTIST_ID).build();
        given(artistRepository.findByUserId(CURRENT_USER_ID)).willReturn(Optional.of(artist));
        given(userRepository.searchChatUsers(
                eq(CURRENT_USER_ID),
                eq(null),
                eq(List.of(Role.CUSTOMER, Role.ARTIST, Role.SHOP)),
                eq(Role.SHOP),
                eq(UserStatus.ACTIVE),
                eq(ContractStatus.PENDING),
                eq(ContractStatus.APPROVED),
                any(LocalDate.class),
                eq(CURRENT_ARTIST_ID),
                eq(pageable)
        )).willReturn(new PageImpl<>(List.of(
                row(SHOP_USER_ID, "계약 대기 샵", Role.SHOP, Specialty.CERAMIC, true, false),
                row(SHOP_USER_ID + 1, "계약 완료 샵", Role.SHOP, Specialty.LIVING_GOODS, false, true),
                row(SHOP_USER_ID + 2, "미계약 샵", Role.SHOP, Specialty.DESK_OFFICE, false, false),
                row(CUSTOMER_USER_ID, "일반 사용자", Role.CUSTOMER, null, false, false)
        ), pageable, 4));

        // when
        Page<ChatUserSearchResponse> response = chatUserSearchService.search(CURRENT_USER_ID, " ", pageable);

        // then
        assertThat(response.getContent()).extracting(ChatUserSearchResponse::getStatusLabel)
                .containsExactly("계약 대기", "계약 완료", "미계약", "-");
    }

    private ChatUserSearchRow row(
            Long userId,
            String accountName,
            Role role,
            Specialty specialty,
            boolean pendingContractExists,
            boolean approvedContractExists
    ) {
        return new ChatUserSearchRow() {
            @Override
            public Long getUserId() {
                return userId;
            }

            @Override
            public String getAccountName() {
                return accountName;
            }

            @Override
            public Role getRole() {
                return role;
            }

            @Override
            public String getProfileImageUrl() {
                return null;
            }

            @Override
            public Specialty getSpecialty() {
                return specialty;
            }

            @Override
            public Boolean getPendingContractExists() {
                return pendingContractExists;
            }

            @Override
            public Boolean getApprovedContractExists() {
                return approvedContractExists;
            }
        };
    }
}
