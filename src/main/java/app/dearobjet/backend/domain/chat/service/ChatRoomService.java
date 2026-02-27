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
import app.dearobjet.backend.domain.user.repository.UserRepository;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.ErrorCode;
import app.dearobjet.backend.global.exception.InvalidInputException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 채팅방 비즈니스 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final UserRepository userRepository;
    private final UnreadCountRedisService unreadCountRedisService;

    /**
     * 채팅방 생성 또는 기존 채팅방 반환
     *
     * @param currentUserId 현재 사용자 ID
     * @param request       채팅방 생성 요청
     * @return 채팅방 정보
     */
    @Transactional
    public ChatRoomResponse createOrGetChatRoom(Long currentUserId, CreateChatRoomRequest request) {
        // 참여자 목록에 현재 사용자 추가
        List<Long> allParticipantIds = new java.util.ArrayList<>(request.getParticipantIds());
        if (!allParticipantIds.contains(currentUserId)) {
            allParticipantIds.add(currentUserId);
        }

        // 1:1 채팅의 경우 기존 채팅방 확인
        if (request.getType() == ChatRoomType.ONE_TO_ONE && allParticipantIds.size() == 2) {
            String participantHash = generateParticipantHash(allParticipantIds);
            return chatRoomRepository.findByParticipantHash(participantHash)
                    .map(room -> {
                        ChatParticipant myParticipation = chatParticipantRepository
                                .findByChatRoomIdAndUserId(room.getId(), currentUserId)
                                .orElse(null);
                        return ChatRoomResponse.from(room, myParticipation, currentUserId);
                    })
                    .orElseGet(() -> createNewChatRoom(currentUserId, allParticipantIds, request.getType()));
        }

        return createNewChatRoom(currentUserId, allParticipantIds, request.getType());
    }

    /**
     * 새 채팅방 생성
     */
    @Transactional
    protected ChatRoomResponse createNewChatRoom(Long currentUserId, List<Long> participantIds, ChatRoomType type) {
        List<User> participants = userRepository.findAllById(participantIds);

        if (participants.size() != participantIds.size()) {
            throw new InvalidInputException(ErrorCode.INVALID_CHAT_PARTICIPANTS);
        }

        ChatRoom chatRoom;
        if (type == ChatRoomType.ONE_TO_ONE) {
            User currentUser = participants.stream()
                    .filter(u -> u.getId().equals(currentUserId))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

            User partner = participants.stream()
                    .filter(u -> !u.getId().equals(currentUserId))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

            chatRoom = ChatRoom.createOneToOne(currentUser, partner);
        } else {
            chatRoom = ChatRoom.createGroup(participants);
        }

        chatRoom = chatRoomRepository.save(chatRoom);

        ChatParticipant myParticipation = chatParticipantRepository
                .findByChatRoomIdAndUserId(chatRoom.getId(), currentUserId)
                .orElse(null);

        log.info("Created new chat room - roomId: {}, type: {}, participants: {}",
                chatRoom.getRoomId(), type, participantIds);

        return ChatRoomResponse.from(chatRoom, myParticipation, currentUserId);
    }

    /**
     * 내 채팅방 목록 조회
     *
     * @param userId 사용자 ID
     * @return 채팅방 목록
     */
    public List<ChatRoomResponse> getMyChatRooms(Long userId) {
        List<ChatParticipant> participations = chatParticipantRepository
                .findMyParticipationsWithDetails(userId);

        return participations.stream()
                .map(participation -> {
                    ChatRoom chatRoom = participation.getChatRoom();

                    // Redis에서 unread 카운트 조회 (캐시 우선)
                    int unreadCount = unreadCountRedisService.getUnreadCount(userId, chatRoom.getRoomId());
                    if (unreadCount == 0) {
                        // 캐시 미스 시 DB 값 사용 및 캐시 워밍업
                        unreadCount = participation.getUnreadCount();
                        if (unreadCount > 0) {
                            unreadCountRedisService.warmUp(userId, chatRoom.getRoomId(), unreadCount);
                        }
                    }

                    // 임시로 participation의 unreadCount를 Redis 값으로 설정
                    ChatRoomResponse dto = ChatRoomResponse.from(chatRoom, participation, userId);
                    return ChatRoomResponse.builder()
                            .id(dto.getId())
                            .roomId(dto.getRoomId())
                            .type(dto.getType())
                            .lastMessage(dto.getLastMessage())
                            .lastMessageAt(dto.getLastMessageAt())
                            .unreadCount(unreadCount)
                            .participants(dto.getParticipants())
                            .partnerName(dto.getPartnerName())
                            .partnerProfileImage(dto.getPartnerProfileImage())
                            .build();
                })
                .toList();
    }

    /**
     * 채팅방 상세 조회
     *
     * @param roomId        채팅방 UUID
     * @param currentUserId 현재 사용자 ID
     * @return 채팅방 정보
     */
    public ChatRoomResponse getChatRoom(String roomId, Long currentUserId) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomIdWithParticipants(roomId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 참여자인지 확인
        ChatParticipant myParticipation = chatRoom.getParticipants().stream()
                .filter(p -> p.getUser().getId().equals(currentUserId))
                .findFirst()
                .orElseThrow(() -> new InvalidInputException(ErrorCode.NOT_CHAT_PARTICIPANT));

        return ChatRoomResponse.from(chatRoom, myParticipation, currentUserId);
    }

    /**
     * 채팅방 참여 여부 확인
     *
     * @param roomId 채팅방 UUID
     * @param userId 사용자 ID
     * @return 참여 여부
     */
    public boolean isParticipant(String roomId, Long userId) {
        return chatParticipantRepository.findByRoomIdAndUserId(roomId, userId).isPresent();
    }

    /**
     * 채팅방 참여자 ID 목록 조회
     *
     * @param roomId 채팅방 UUID
     * @return 참여자 ID 목록
     */
    public List<Long> getParticipantIds(String roomId) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomIdWithParticipants(roomId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        return chatRoom.getParticipants().stream()
                .map(p -> p.getUser().getId())
                .toList();
    }

    private String generateParticipantHash(List<Long> userIds) {
        String sortedIds = userIds.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        return DigestUtils.md5DigestAsHex(sortedIds.getBytes(StandardCharsets.UTF_8));
    }
}
