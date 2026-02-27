package app.dearobjet.backend.domain.chat.service;

import app.dearobjet.backend.domain.chat.dto.UserPresenceDto;
import app.dearobjet.backend.domain.chat.entity.ChatParticipant;
import app.dearobjet.backend.domain.chat.repository.ChatParticipantRepository;
import app.dearobjet.backend.domain.chat.service.redis.PresenceRedisService;
import app.dearobjet.backend.domain.chat.service.redis.UnreadCountRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 채팅 참여자 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatParticipantService {

    private final ChatParticipantRepository chatParticipantRepository;
    private final UnreadCountRedisService unreadCountRedisService;
    private final PresenceRedisService presenceRedisService;

    /**
     * 참여자 정보 조회
     *
     * @param roomId 채팅방 UUID
     * @param userId 사용자 ID
     * @return 참여자 정보 (없으면 null)
     */
    public ChatParticipant getParticipant(String roomId, Long userId) {
        return chatParticipantRepository.findByRoomIdAndUserId(roomId, userId).orElse(null);
    }

    /**
     * 채팅방의 모든 참여자 조회
     *
     * @param chatRoomId 채팅방 DB ID
     * @return 참여자 목록
     */
    public List<ChatParticipant> getParticipants(Long chatRoomId) {
        return chatParticipantRepository.findAllByRoomId(chatRoomId);
    }

    /**
     * 읽지 않은 메시지 수 조회 (Redis 우선, DB 폴백)
     *
     * @param userId 사용자 ID
     * @param roomId 채팅방 UUID
     * @return 읽지 않은 메시지 수
     */
    public int getUnreadCount(Long userId, String roomId) {
        // Redis에서 먼저 조회
        int redisCount = unreadCountRedisService.getUnreadCount(userId, roomId);
        if (redisCount > 0) {
            return redisCount;
        }

        // Redis에 없으면 DB에서 조회
        ChatParticipant participant = chatParticipantRepository.findByRoomIdAndUserId(roomId, userId)
                .orElse(null);

        if (participant == null) {
            return 0;
        }

        int dbCount = participant.getUnreadCount();

        // DB 값이 있으면 Redis에 캐시
        if (dbCount > 0) {
            unreadCountRedisService.warmUp(userId, roomId, dbCount);
        }

        return dbCount;
    }

    /**
     * 사용자의 전체 읽지 않은 메시지 수 조회
     *
     * @param userId 사용자 ID
     * @return 전체 읽지 않은 메시지 수
     */
    public int getTotalUnreadCount(Long userId) {
        // Redis에서 조회
        int redisTotal = unreadCountRedisService.getTotalUnreadCount(userId);
        if (redisTotal > 0) {
            return redisTotal;
        }

        // Redis에 없으면 DB에서 계산
        List<ChatParticipant> participations = chatParticipantRepository
                .findMyParticipationsWithDetails(userId);

        return participations.stream()
                .mapToInt(ChatParticipant::getUnreadCount)
                .sum();
    }

    /**
     * 채팅방의 온라인 사용자 목록 조회
     *
     * @param roomId 채팅방 UUID
     * @return 온라인 사용자 ID 목록
     */
    public Set<Long> getOnlineUsersInRoom(String roomId) {
        return presenceRedisService.getOnlineUsersInRoom(roomId);
    }

    /**
     * 사용자 온라인 상태 조회
     *
     * @param userId 사용자 ID
     * @return 온라인 상태
     */
    public UserPresenceDto.Status getUserPresence(Long userId) {
        return presenceRedisService.getPresence(userId);
    }

    /**
     * Redis -> DB unread 카운트 동기화 (배치)
     * 주기적으로 Redis의 unread 카운트를 DB에 동기화
     */
    /**
     * Redis -> DB unread 카운트 동기화 (배치)
     * dirty flag가 있는 항목만 동기화하여 불필요한 DB 쿼리 최소화
     */
    @Scheduled(fixedRate = 300000) // 5분마다
    @Transactional
    public void syncUnreadCountsToDb() {
        Set<String> dirtyEntries = unreadCountRedisService.popDirtyEntries();
        if (dirtyEntries.isEmpty()) {
            return;
        }

        log.debug("Syncing {} dirty unread count entries to DB", dirtyEntries.size());

        int syncedCount = 0;
        for (String entry : dirtyEntries) {
            try {
                // "userId:roomId" 형태 파싱
                String[] parts = entry.split(":", 2);
                if (parts.length != 2) {
                    log.warn("Invalid dirty entry format: {}", entry);
                    continue;
                }

                Long userId = Long.parseLong(parts[0]);
                String roomId = parts[1];

                int redisCount = unreadCountRedisService.getUnreadCount(userId, roomId);

                chatParticipantRepository.findByRoomIdAndUserId(roomId, userId)
                        .ifPresent(participant -> participant.updateUnreadCount(redisCount));

                syncedCount++;
            } catch (NumberFormatException e) {
                log.warn("Invalid userId in dirty entry: {}", entry);
            }
        }

        log.debug("Unread count sync completed: {}/{} entries", syncedCount, dirtyEntries.size());
    }
}
