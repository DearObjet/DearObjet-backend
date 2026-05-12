package app.dearobjet.backend.domain.chat.service;

import app.dearobjet.backend.domain.chat.dto.ChatRoomResponse;
import app.dearobjet.backend.domain.chat.dto.CreateChatRoomRequest;
import app.dearobjet.backend.domain.chat.entity.ChatParticipant;
import app.dearobjet.backend.domain.chat.entity.ChatRoom;
import app.dearobjet.backend.domain.chat.entity.ChatRoomType;
import app.dearobjet.backend.domain.chat.repository.ChatParticipantRepository;
import app.dearobjet.backend.domain.chat.repository.ChatRoomRepository;
import app.dearobjet.backend.domain.chat.service.redis.UnreadCountRedisService;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.enums.UserStatus;
import app.dearobjet.backend.domain.user.repository.UserRepository;
import app.dearobjet.backend.global.exception.ErrorCode;
import app.dearobjet.backend.global.exception.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DisplayName("ChatRoomService 테스트")
@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @InjectMocks
    private ChatRoomService chatRoomService;

    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private ChatParticipantRepository chatParticipantRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UnreadCountRedisService unreadCountRedisService;

    private User currentUser;
    private User partner;

    private static final Long CURRENT_USER_ID = 1L;
    private static final Long PARTNER_ID = 2L;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .id(CURRENT_USER_ID)
                .name("Alice")
                .email("alice@test.com")
                .role(Role.CUSTOMER)
                .userStatus(UserStatus.ACTIVE)
                .build();

        partner = User.builder()
                .id(PARTNER_ID)
                .name("Bob")
                .email("bob@test.com")
                .role(Role.CUSTOMER)
                .userStatus(UserStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("createOrGetChatRoom")
    class CreateOrGetChatRoomTest {

        @Test
        @DisplayName("기존 1:1 채팅방 존재 시 새로 생성하지 않고 기존 채팅방 반환")
        void givenExistingOneToOneRoom_whenCreate_thenReturnExisting() {
            // given
            CreateChatRoomRequest request = CreateChatRoomRequest.oneToOne(PARTNER_ID);

            ChatParticipant myParticipation = ChatParticipant.builder()
                    .id(1L).user(currentUser).unreadCount(0).build();
            ChatParticipant partnerParticipation = ChatParticipant.builder()
                    .id(2L).user(partner).unreadCount(0).build();

            ChatRoom existingRoom = ChatRoom.builder()
                    .id(1L)
                    .roomId("existing-room-uuid")
                    .type(ChatRoomType.ONE_TO_ONE)
                    .participantHash("test-hash")
                    .lastMessage("")
                    .participants(List.of(myParticipation, partnerParticipation))
                    .build();

            given(chatRoomRepository.findByParticipantHash(anyString()))
                    .willReturn(Optional.of(existingRoom));
            given(chatParticipantRepository.findByChatRoomIdAndUserId(1L, CURRENT_USER_ID))
                    .willReturn(Optional.of(myParticipation));

            // when
            ChatRoomResponse result = chatRoomService.createOrGetChatRoom(CURRENT_USER_ID, request);

            // then
            assertThat(result.getRoomId()).isEqualTo("existing-room-uuid");
            // 새 채팅방 저장이 호출되지 않음
            verify(chatRoomRepository, never()).save(any());
        }

        @Test
        @DisplayName("기존 1:1 채팅방 없으면 새로 생성")
        void givenNoExistingRoom_whenCreate_thenCreateNew() {
            // given
            CreateChatRoomRequest request = CreateChatRoomRequest.oneToOne(PARTNER_ID);

            given(chatRoomRepository.findByParticipantHash(anyString()))
                    .willReturn(Optional.empty());
            given(userRepository.findAllById(anyList()))
                    .willReturn(List.of(currentUser, partner));
            given(chatRoomRepository.save(any(ChatRoom.class)))
                    .willAnswer(invocation -> {
                        ChatRoom room = invocation.getArgument(0);
                        // save 후 ID 부여 시뮬레이션
                        return ChatRoom.builder()
                                .id(1L)
                                .roomId(room.getRoomId())
                                .type(room.getType())
                                .participantHash(room.getParticipantHash())
                                .lastMessage(room.getLastMessage())
                                .participants(room.getParticipants())
                                .build();
                    });
            given(chatParticipantRepository.findByChatRoomIdAndUserId(eq(1L), eq(CURRENT_USER_ID)))
                    .willReturn(Optional.empty());

            // when
            ChatRoomResponse result = chatRoomService.createOrGetChatRoom(CURRENT_USER_ID, request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getType()).isEqualTo(ChatRoomType.ONE_TO_ONE);
            verify(chatRoomRepository).save(any(ChatRoom.class));
        }

        @Test
        @DisplayName("자기 자신과 채팅방 생성 시 예외")
        void givenSameUser_whenCreate_thenThrowException() {
            // given
            CreateChatRoomRequest request = CreateChatRoomRequest.oneToOne(CURRENT_USER_ID);

            // when & then
            assertThatThrownBy(() -> chatRoomService.createOrGetChatRoom(CURRENT_USER_ID, request))
                    .isInstanceOf(InvalidInputException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.CANNOT_CHAT_WITH_SELF);
        }
    }
}
