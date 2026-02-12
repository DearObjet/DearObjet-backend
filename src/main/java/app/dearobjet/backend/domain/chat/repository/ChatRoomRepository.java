package app.dearobjet.backend.domain.chat.repository;

import app.dearobjet.backend.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 채팅방 Repository
 */
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * participantHash로 중복 채팅방 조회
     *
     * @param participantHash 참여자 해시값
     * @return 채팅방
     */
    Optional<ChatRoom> findByParticipantHash(String participantHash);

    /**
     * roomId(UUID)로 채팅방 조회
     * API에서 외부 식별자로 조회
     *
     * @param roomId 채팅방 UUID
     * @return 채팅방
     */
    Optional<ChatRoom> findByRoomId(String roomId);

    /**
     * 채팅방 UUID로 참여자 정보를 함께 조회
     *
     * @param roomId 채팅방 UUID
     * @return 채팅방 + 참여자 정보
     */
    @Query("SELECT cr FROM ChatRoom cr " +
            "LEFT JOIN FETCH cr.participants p " +
            "LEFT JOIN FETCH p.user " +
            "WHERE cr.roomId = :roomId")
    Optional<ChatRoom> findByRoomIdWithParticipants(@Param("roomId") String roomId);

    /**
     * 채팅방 ID로 참여자 정보를 함께 조회
     *
     * @param chatRoomId 채팅방 DB ID
     * @return 채팅방 + 참여자 정보
     */
    @Query("SELECT cr FROM ChatRoom cr " +
            "LEFT JOIN FETCH cr.participants p " +
            "LEFT JOIN FETCH p.user " +
            "WHERE cr.id = :chatRoomId")
    Optional<ChatRoom> findByIdWithParticipants(@Param("chatRoomId") Long chatRoomId);
}