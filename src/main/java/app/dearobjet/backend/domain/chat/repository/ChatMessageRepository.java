package app.dearobjet.backend.domain.chat.repository;

import app.dearobjet.backend.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 채팅 메시지 Repository
 */
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 채팅방 메시지 페이징 조회 (최신순)
     *
     * @param roomId   채팅방 UUID
     * @param pageable 페이지 정보
     * @return 메시지 페이지
     */
    Page<ChatMessage> findByRoomIdOrderByCreatedAtDesc(String roomId, Pageable pageable);

    /**
     * 특정 시점 이후 메시지 조회
     * 읽지 않은 메시지 목록 조회에 사용
     *
     * @param roomId    채팅방 UUID
     * @param createdAt 기준 시점
     * @return 메시지 목록
     */
    @Query("SELECT m FROM ChatMessage m " +
            "WHERE m.roomId = :roomId " +
            "AND m.createdAt > :createdAt " +
            "ORDER BY m.createdAt ASC")
    List<ChatMessage> findByRoomIdAndCreatedAtAfter(@Param("roomId") String roomId,
                                                     @Param("createdAt") LocalDateTime createdAt);

    /**
     * 특정 시점 이후 메시지 수 조회
     * 읽지 않은 메시지 수 계산에 사용
     *
     * @param roomId    채팅방 UUID
     * @param createdAt 기준 시점
     * @return 메시지 수
     */
    @Query("SELECT COUNT(m) FROM ChatMessage m " +
            "WHERE m.roomId = :roomId " +
            "AND m.createdAt > :createdAt")
    long countByRoomIdAndCreatedAtAfter(@Param("roomId") String roomId,
                                        @Param("createdAt") LocalDateTime createdAt);
}