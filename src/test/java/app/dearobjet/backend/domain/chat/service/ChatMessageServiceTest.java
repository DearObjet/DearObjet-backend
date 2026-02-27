package app.dearobjet.backend.domain.chat.service;

import app.dearobjet.backend.domain.chat.dto.ChatMessageResponse;
import app.dearobjet.backend.domain.chat.dto.SendMessageRequest;
import app.dearobjet.backend.domain.chat.entity.*;
import app.dearobjet.backend.domain.chat.repository.ChatMessageRepository;
import app.dearobjet.backend.domain.chat.repository.ChatParticipantRepository;
import app.dearobjet.backend.domain.chat.repository.ChatRoomRepository;
import app.dearobjet.backend.domain.chat.service.redis.ChatMessagePublisher;
import app.dearobjet.backend.domain.chat.service.redis.MessageCacheRedisService;
import app.dearobjet.backend.domain.chat.service.redis.UnreadCountRedisService;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.enums.UserStatus;
import app.dearobjet.backend.domain.user.repository.UserRepository;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.InvalidInputException;
import app.dearobjet.backend.global.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@DisplayName("ChatMessageService 테스트")
@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    @InjectMocks
    private ChatMessageService chatMessageService;

    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private ChatParticipantRepository chatParticipantRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ChatMessagePublisher messagePublisher;
    @Mock
    private UnreadCountRedisService unreadCountRedisService;
    @Mock
    private MessageCacheRedisService messageCacheRedisService;

    private User sender;
    private User receiver;
    private ChatRoom chatRoom;
    private ChatParticipant senderParticipant;
    private ChatParticipant receiverParticipant;

    private static final String ROOM_ID = "test-room-uuid";
    private static final Long SENDER_ID = 1L;
    private static final Long RECEIVER_ID = 2L;

    @BeforeEach
    void setUp() {
        sender = User.builder()
                .id(SENDER_ID)
                .name("Alice")
                .email("alice@test.com")
                .role(Role.CUSTOMER)
                .userStatus(UserStatus.ACTIVE)
                .build();

        receiver = User.builder()
                .id(RECEIVER_ID)
                .name("Bob")
                .email("bob@test.com")
                .role(Role.CUSTOMER)
                .userStatus(UserStatus.ACTIVE)
                .build();

        senderParticipant = ChatParticipant.builder()
                .id(1L)
                .user(sender)
                .unreadCount(0)
                .build();

        receiverParticipant = ChatParticipant.builder()
                .id(2L)
                .user(receiver)
                .unreadCount(0)
                .build();

        chatRoom = ChatRoom.builder()
                .id(1L)
                .roomId(ROOM_ID)
                .type(ChatRoomType.ONE_TO_ONE)
                .participantHash("test-hash")
                .lastMessage("")
                .participants(List.of(senderParticipant, receiverParticipant))
                .build();
    }

    @Nested
    @DisplayName("sendMessage")
    class SendMessageTest {

        @Test
        @DisplayName("정상 요청 시 메시지 저장 후 Redis 발행")
        void givenValidRequest_whenSendMessage_thenSaveAndPublish() {
            // given
            SendMessageRequest request = SendMessageRequest.ofText(ROOM_ID, "안녕하세요");

            given(chatRoomRepository.findByRoomIdWithParticipants(ROOM_ID))
                    .willReturn(Optional.of(chatRoom));
            given(userRepository.findById(SENDER_ID))
                    .willReturn(Optional.of(sender));

            ChatMessage savedMessage = ChatMessage.builder()
                    .id(100L)
                    .roomId(ROOM_ID)
                    .chatRoom(chatRoom)
                    .sender(sender)
                    .content("안녕하세요")
                    .messageType(MessageType.TEXT)
                    .createdAt(LocalDateTime.now())
                    .build();
            given(chatMessageRepository.save(any(ChatMessage.class)))
                    .willReturn(savedMessage);

            // when
            ChatMessageResponse result = chatMessageService.sendMessage(SENDER_ID, ROOM_ID, request);

            // then
            assertThat(result.getContent()).isEqualTo("안녕하세요");
            assertThat(result.getSenderId()).isEqualTo(SENDER_ID);
            verify(messagePublisher).publishToRoom(any());
            verify(unreadCountRedisService).incrementForParticipants(eq(ROOM_ID), anySet(), eq(SENDER_ID));
            verify(messageCacheRedisService).addRecentMessage(any(), eq(SENDER_ID));
        }

        @Test
        @DisplayName("존재하지 않는 채팅방이면 EntityNotFoundException")
        void givenNonExistentRoom_whenSendMessage_thenThrowEntityNotFound() {
            // given
            SendMessageRequest request = SendMessageRequest.ofText(ROOM_ID, "test");
            given(chatRoomRepository.findByRoomIdWithParticipants(ROOM_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> chatMessageService.sendMessage(SENDER_ID, ROOM_ID, request))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("비참여자가 메시지 전송 시 InvalidInputException")
        void givenNonParticipant_whenSendMessage_thenThrowInvalidInput() {
            // given
            Long nonParticipantId = 999L;
            SendMessageRequest request = SendMessageRequest.ofText(ROOM_ID, "test");
            given(chatRoomRepository.findByRoomIdWithParticipants(ROOM_ID))
                    .willReturn(Optional.of(chatRoom));

            // when & then
            assertThatThrownBy(() -> chatMessageService.sendMessage(nonParticipantId, ROOM_ID, request))
                    .isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("userId가 null이면 UnauthorizedException")
        void givenNullUserId_whenSendMessage_thenThrowUnauthorized() {
            // given
            SendMessageRequest request = SendMessageRequest.ofText(ROOM_ID, "test");

            // when & then
            assertThatThrownBy(() -> chatMessageService.sendMessage(null, ROOM_ID, request))
                    .isInstanceOf(UnauthorizedException.class);
        }
    }

    @Nested
    @DisplayName("markAsRead")
    class MarkAsReadTest {

        @Test
        @DisplayName("정상 요청 시 unread 카운트 초기화 및 읽음 이벤트 발행")
        void givenValidRequest_whenMarkAsRead_thenResetUnreadCount() {
            // given
            ChatParticipant participant = ChatParticipant.builder()
                    .id(1L)
                    .user(sender)
                    .unreadCount(5)
                    .build();

            given(chatParticipantRepository.findByRoomIdAndUserId(ROOM_ID, SENDER_ID))
                    .willReturn(Optional.of(participant));

            // when
            chatMessageService.markAsRead(SENDER_ID, ROOM_ID);

            // then
            verify(unreadCountRedisService).markAsRead(SENDER_ID, ROOM_ID);
            verify(messagePublisher).publishToRoom(any());
            assertThat(participant.getUnreadCount()).isZero();
        }
    }

    @Nested
    @DisplayName("getMessages")
    class GetMessagesTest {

        @Test
        @DisplayName("첫 페이지에 캐시가 있으면 캐시에서 조회")
        void givenFirstPage_whenGetMessages_thenUseCacheIfAvailable() {
            // given
            ChatMessageResponse cachedDto = ChatMessageResponse.builder()
                    .id(1L)
                    .roomId(ROOM_ID)
                    .content("cached")
                    .build();

            given(chatParticipantRepository.findByRoomIdAndUserId(ROOM_ID, SENDER_ID))
                    .willReturn(Optional.of(senderParticipant));
            given(messageCacheRedisService.hasCache(ROOM_ID)).willReturn(true);
            given(messageCacheRedisService.getRecentMessages(ROOM_ID, 20))
                    .willReturn(List.of(cachedDto));

            // when
            List<ChatMessageResponse> result = chatMessageService.getMessages(ROOM_ID, SENDER_ID, 0, 20);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getContent()).isEqualTo("cached");
            // DB 조회하지 않음
            verify(chatMessageRepository, never()).findByRoomIdOrderByCreatedAtDesc(anyString(), any());
        }

        @Test
        @DisplayName("캐시 미스 시 DB에서 조회")
        void givenNoCachedMessages_whenGetMessages_thenQueryDb() {
            // given
            given(chatParticipantRepository.findByRoomIdAndUserId(ROOM_ID, SENDER_ID))
                    .willReturn(Optional.of(senderParticipant));
            given(messageCacheRedisService.hasCache(ROOM_ID)).willReturn(false);

            ChatMessage dbMessage = ChatMessage.builder()
                    .id(1L)
                    .roomId(ROOM_ID)
                    .sender(sender)
                    .content("db message")
                    .messageType(MessageType.TEXT)
                    .createdAt(LocalDateTime.now())
                    .build();
            given(chatMessageRepository.findByRoomIdOrderByCreatedAtDesc(eq(ROOM_ID), any()))
                    .willReturn(new PageImpl<>(List.of(dbMessage)));

            // when
            List<ChatMessageResponse> result = chatMessageService.getMessages(ROOM_ID, SENDER_ID, 0, 20);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getContent()).isEqualTo("db message");
        }
    }
}
