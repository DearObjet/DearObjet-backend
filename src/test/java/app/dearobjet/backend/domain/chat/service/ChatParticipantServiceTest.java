package app.dearobjet.backend.domain.chat.service;

import app.dearobjet.backend.domain.chat.entity.ChatParticipant;
import app.dearobjet.backend.domain.chat.entity.ChatRoom;
import app.dearobjet.backend.domain.chat.entity.ChatRoomType;
import app.dearobjet.backend.domain.chat.repository.ChatParticipantRepository;
import app.dearobjet.backend.domain.chat.service.redis.PresenceRedisService;
import app.dearobjet.backend.domain.chat.service.redis.UnreadCountRedisService;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@DisplayName("ChatParticipantService 테스트")
@ExtendWith(MockitoExtension.class)
class ChatParticipantServiceTest {

    @InjectMocks
    private ChatParticipantService chatParticipantService;

    @Mock
    private ChatParticipantRepository chatParticipantRepository;
    @Mock
    private UnreadCountRedisService unreadCountRedisService;
    @Mock
    private PresenceRedisService presenceRedisService;

    private User user;
    private ChatRoom chatRoom;
    private ChatParticipant participant;

    private static final Long USER_ID = 1L;
    private static final String ROOM_ID = "test-room-uuid";

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(USER_ID)
                .name("Alice")
                .email("alice@test.com")
                .role(Role.USER)
                .userStatus(UserStatus.ACTIVE)
                .build();

        chatRoom = ChatRoom.builder()
                .id(1L)
                .roomId(ROOM_ID)
                .type(ChatRoomType.ONE_TO_ONE)
                .participantHash("test-hash")
                .lastMessage("")
                .build();

        participant = ChatParticipant.builder()
                .id(1L)
                .user(user)
                .chatRoom(chatRoom)
                .unreadCount(3)
                .build();
    }

    @Nested
    @DisplayName("getUnreadCount")
    class GetUnreadCountTest {

        @Test
        @DisplayName("Redis에 값이 있으면 Redis 값 반환")
        void givenRedisHasCount_whenGetUnreadCount_thenReturnRedisValue() {
            // given
            given(unreadCountRedisService.getUnreadCount(USER_ID, ROOM_ID))
                    .willReturn(5);

            // when
            int result = chatParticipantService.getUnreadCount(USER_ID, ROOM_ID);

            // then
            assertThat(result).isEqualTo(5);
        }

        @Test
        @DisplayName("Redis에 값이 없으면 DB에서 조회 후 캐시 워밍업")
        void givenRedisEmpty_whenGetUnreadCount_thenFallbackToDbAndWarmUp() {
            // given
            given(unreadCountRedisService.getUnreadCount(USER_ID, ROOM_ID))
                    .willReturn(0);
            given(chatParticipantRepository.findByRoomIdAndUserId(ROOM_ID, USER_ID))
                    .willReturn(Optional.of(participant));

            // when
            int result = chatParticipantService.getUnreadCount(USER_ID, ROOM_ID);

            // then
            assertThat(result).isEqualTo(3);
            // Redis 캐시 워밍업 확인
            verify(unreadCountRedisService).warmUp(USER_ID, ROOM_ID, 3);
        }

        @Test
        @DisplayName("Redis와 DB 모두 없으면 0 반환")
        void givenNoDataAnywhere_whenGetUnreadCount_thenReturnZero() {
            // given
            given(unreadCountRedisService.getUnreadCount(USER_ID, ROOM_ID))
                    .willReturn(0);
            given(chatParticipantRepository.findByRoomIdAndUserId(ROOM_ID, USER_ID))
                    .willReturn(Optional.empty());

            // when
            int result = chatParticipantService.getUnreadCount(USER_ID, ROOM_ID);

            // then
            assertThat(result).isZero();
        }
    }

    @Nested
    @DisplayName("syncUnreadCountsToDb")
    class SyncUnreadCountsToDbTest {

        @Test
        @DisplayName("dirty entries가 있으면 Redis 값으로 DB 업데이트")
        void givenDirtyEntries_whenSync_thenUpdateDb() {
            // given
            Set<String> dirtyEntries = Set.of(USER_ID + ":" + ROOM_ID);
            given(unreadCountRedisService.popDirtyEntries())
                    .willReturn(dirtyEntries);
            given(unreadCountRedisService.getUnreadCount(USER_ID, ROOM_ID))
                    .willReturn(7);
            given(chatParticipantRepository.findByRoomIdAndUserId(ROOM_ID, USER_ID))
                    .willReturn(Optional.of(participant));

            // when
            chatParticipantService.syncUnreadCountsToDb();

            // then
            assertThat(participant.getUnreadCount()).isEqualTo(7);
        }

        @Test
        @DisplayName("dirty entries가 없으면 아무 작업도 하지 않음")
        void givenNoDirtyEntries_whenSync_thenDoNothing() {
            // given
            given(unreadCountRedisService.popDirtyEntries())
                    .willReturn(Set.of());

            // when
            chatParticipantService.syncUnreadCountsToDb();

            // then
            // DB 조회가 발생하지 않아야 함
            verify(chatParticipantRepository, org.mockito.Mockito.never())
                    .findByRoomIdAndUserId(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyLong());
        }
    }
}
