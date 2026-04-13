package app.dearobjet.backend.domain.chat.service;

import app.dearobjet.backend.domain.chat.dto.ChatMessageResponse;
import app.dearobjet.backend.domain.chat.dto.ChatMessageCursorResponse;
import app.dearobjet.backend.domain.chat.dto.RedisChatMessageDto;
import app.dearobjet.backend.domain.chat.dto.SendMessageRequest;
import app.dearobjet.backend.domain.chat.entity.ChatMessage;
import app.dearobjet.backend.domain.chat.entity.ChatParticipant;
import app.dearobjet.backend.domain.chat.entity.ChatRoom;
import app.dearobjet.backend.domain.chat.repository.ChatMessageRepository;
import app.dearobjet.backend.domain.chat.repository.ChatParticipantRepository;
import app.dearobjet.backend.domain.chat.repository.ChatRoomRepository;
import app.dearobjet.backend.domain.chat.service.redis.ChatMessagePublisher;
import app.dearobjet.backend.domain.chat.service.redis.MessageCacheRedisService;
import app.dearobjet.backend.domain.chat.service.redis.UnreadCountRedisService;
import app.dearobjet.backend.domain.chat.service.redis.PresenceRedisService;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.repository.UserRepository;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.ErrorCode;
import app.dearobjet.backend.global.exception.InvalidInputException;
import app.dearobjet.backend.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 채팅 메시지 비즈니스 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final UserRepository userRepository;
    private final ChatMessagePublisher messagePublisher;
    private final UnreadCountRedisService unreadCountRedisService;
    private final MessageCacheRedisService messageCacheRedisService;
    private final PresenceRedisService presenceRedisService;

    /**
     * 메시지 전송
     *
     * @param senderId 발신자 ID
     * @param roomId   채팅방 UUID
     * @param request  메시지 전송 요청
     * @return 저장된 메시지 DTO
     */
    @Transactional
    public ChatMessageResponse sendMessage(Long senderId, String roomId, SendMessageRequest request) {
        validateAuthenticated(senderId);
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findByRoomIdWithParticipants(roomId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 참여자 확인
        boolean isParticipant = chatRoom.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(senderId));
        if (!isParticipant) {
            throw new InvalidInputException(ErrorCode.NOT_CHAT_PARTICIPANT);
        }

        // 발신자 조회
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        // 메시지 생성
        ChatMessage message = ChatMessage.builder()
                .roomId(roomId)
                .chatRoom(chatRoom)
                .sender(sender)
                .content(request.getContent())
                .messageType(request.getMessageType())
                .createdAt(LocalDateTime.now())
                .build();

        // DB 저장
        message = chatMessageRepository.save(message);

        // 채팅방 마지막 메시지 업데이트
        chatRoom.updateLastMessage(request.getContent());

        // 발신자 제외한 참여자들의 unread 카운트 증가 (Redis)
        Set<Long> participantIds = chatRoom.getParticipants().stream()
                .map(p -> p.getUser().getId())
                .collect(Collectors.toSet());

        // 현재 채팅방에 접속 중인 유저 조회
        Set<Long> activeUsers = presenceRedisService.getOnlineUsersInRoom(roomId);

        // 접속 중인 유저는 unread 증가 제외
        for (Long participantId : participantIds) {
            if (!participantId.equals(senderId) && !activeUsers.contains(participantId)) {
                unreadCountRedisService.incrementUnreadCount(participantId, roomId);
            }
        }

        // DB에도 unread 카운트 증가 (정합성 유지)
        chatRoom.incrementUnreadCountExcluding(senderId, activeUsers);

        // Redis 캐시에 메시지 추가
        messageCacheRedisService.addRecentMessage(message, senderId);

        // Redis Pub/Sub으로 메시지 브로드캐스트
        RedisChatMessageDto redisMessage = RedisChatMessageDto.ofMessage(
                roomId,
                message.getId(),
                senderId,
                sender.getName(),
                sender.getProfileImage(),
                request.getContent(),
                request.getMessageType(),
                message.getCreatedAt()
        );
        messagePublisher.publishToRoom(redisMessage);

        log.debug("Message sent - roomId: {}, senderId: {}, messageId: {}",
                roomId, senderId, message.getId());

        return ChatMessageResponse.from(message, senderId);
    }

    /**
     * 채팅방 메시지 히스토리 조회
     *
     * @param roomId        채팅방 UUID
     * @param currentUserId 현재 사용자 ID
     * @param page          페이지 번호
     * @param size          페이지 크기
     * @return 메시지 목록
     */
    public List<ChatMessageResponse> getMessages(String roomId, Long currentUserId, int page, int size) {
        validateAuthenticated(currentUserId);
        validateParticipant(roomId, currentUserId);

        // 첫 페이지이고 캐시가 있으면 캐시에서 조회
        if (page == 0 && messageCacheRedisService.hasCache(roomId)) {
            List<ChatMessageResponse> cachedMessages = messageCacheRedisService.getRecentMessages(roomId, size);
            if (!cachedMessages.isEmpty()) {
                log.debug("Messages retrieved from cache - roomId: {}, count: {}", roomId, cachedMessages.size());
                return cachedMessages;
            }
        }

        // DB에서 조회
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessage> messagePage = chatMessageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, pageable);

        List<ChatMessageResponse> messages = messagePage.getContent().stream()
                .map(m -> ChatMessageResponse.from(m, currentUserId))
                .toList();

        // 첫 페이지면 캐시 워밍업
        if (page == 0 && !messagePage.isEmpty()) {
            messageCacheRedisService.warmUp(roomId, messagePage.getContent(), currentUserId);
        }

        return messages;
    }

    /**
     * 읽음 처리
     *
     * @param userId 사용자 ID
     * @param roomId 채팅방 UUID
     */
    @Transactional
    public void markAsRead(Long userId, String roomId) {
        validateAuthenticated(userId);
        // 참여자 조회
        ChatParticipant participant = chatParticipantRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new InvalidInputException(ErrorCode.NOT_CHAT_PARTICIPANT));

        // Redis에서 읽음 처리 (카운트 초기화)
        unreadCountRedisService.markAsRead(userId, roomId);

        // DB에서도 읽음 처리
        participant.markAsRead();

        // 읽음 이벤트 브로드캐스트
        RedisChatMessageDto readEvent = RedisChatMessageDto.ofRead(roomId, userId, LocalDateTime.now());
        messagePublisher.publishToRoom(readEvent);

        log.debug("Marked as read - roomId: {}, userId: {}", roomId, userId);
    }

    /**
     * 특정 시점 이후 읽지 않은 메시지 수 조회
     *
     * @param roomId 채팅방 UUID
     * @param since  기준 시점
     * @return 읽지 않은 메시지 수
     */
    public long getUnreadMessageCount(String roomId, LocalDateTime since) {
        return chatMessageRepository.countByRoomIdAndCreatedAtAfter(roomId, since);
    }

    /**
     * 방 진입 시 최신 메시지 N개 조회 (커서 초기화)
     */
    public ChatMessageCursorResponse getLatestMessages(String roomId, Long currentUserId, int limit) {
        validateAuthenticated(currentUserId);
        validateParticipant(roomId, currentUserId);

        int size = normalizeLimit(limit, 50, 200);
        List<ChatMessage> desc = chatMessageRepository
                .findByRoomIdOrderByIdDesc(roomId, PageRequest.of(0, size))
                .getContent();

        List<ChatMessage> asc = new ArrayList<>(desc);
        Collections.reverse(asc);

        List<ChatMessageResponse> messages = asc.stream()
                .map(m -> ChatMessageResponse.from(m, currentUserId))
                .toList();

        return toCursorResponse(roomId, messages);
    }

    /**
     * 재접속/누락 복구: afterMessageId 이후 메시지 조회
     */
    public ChatMessageCursorResponse syncMessages(String roomId, Long currentUserId, Long afterMessageId, int limit) {
        validateAuthenticated(currentUserId);
        validateParticipant(roomId, currentUserId);

        long after = afterMessageId != null ? afterMessageId : 0L;
        int size = normalizeLimit(limit, 200, 500);

        List<ChatMessageResponse> messages = chatMessageRepository
                .findByRoomIdAndIdGreaterThanOrderByIdAsc(roomId, after, PageRequest.of(0, size))
                .getContent()
                .stream()
                .map(m -> ChatMessageResponse.from(m, currentUserId))
                .toList();

        return toCursorResponse(roomId, messages);
    }

    /**
     * 과거 더보기: beforeMessageId 이전 메시지 조회
     */
    public ChatMessageCursorResponse getMessagesBefore(
            String roomId,
            Long currentUserId,
            Long beforeMessageId,
            int limit) {

        validateAuthenticated(currentUserId);
        validateParticipant(roomId, currentUserId);

        if (beforeMessageId == null) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "beforeMessageId는 필수입니다.");
        }

        int size = normalizeLimit(limit, 50, 200);
        List<ChatMessage> desc = chatMessageRepository
                .findByRoomIdAndIdLessThanOrderByIdDesc(roomId, beforeMessageId, PageRequest.of(0, size))
                .getContent();

        List<ChatMessage> asc = new ArrayList<>(desc);
        Collections.reverse(asc);

        List<ChatMessageResponse> messages = asc.stream()
                .map(m -> ChatMessageResponse.from(m, currentUserId))
                .toList();

        return toCursorResponse(roomId, messages);
    }

    private void validateAuthenticated(Long userId) {
        if (userId == null) {
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void validateParticipant(String roomId, Long userId) {
        chatParticipantRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new InvalidInputException(ErrorCode.NOT_CHAT_PARTICIPANT));
    }

    private int normalizeLimit(int limit, int defaultLimit, int maxLimit) {
        if (limit <= 0) {
            return defaultLimit;
        }
        return Math.min(limit, maxLimit);
    }

    private ChatMessageCursorResponse toCursorResponse(String roomId, List<ChatMessageResponse> messages) {
        if (messages.isEmpty()) {
            return new ChatMessageCursorResponse(roomId, null, null, messages);
        }
        Long oldest = messages.get(0).getId();
        Long latest = messages.get(messages.size() - 1).getId();
        return new ChatMessageCursorResponse(roomId, oldest, latest, messages);
    }
}
