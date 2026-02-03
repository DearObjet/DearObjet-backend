package app.dearobjet.backend.domain.chat.repository;

import app.dearobjet.backend.domain.chat.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 채팅방 참여자 Repository
 */
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    /**
     * 내 참여 정보 조회 (채팅방 + 상대방 정보 한 번에)
     *
     * @param userId 사용자 ID
     * @return 내 참여 정보 목록 (채팅방, 상대방 포함)
     */
    @Query("SELECT cp FROM ChatParticipant cp " +
            "JOIN FETCH cp.chatRoom cr " +
            "JOIN FETCH cp.user u " +
            "WHERE u.id = :userId " +
            "ORDER BY cr.updatedAt DESC")
    List<ChatParticipant> findMyParticipationsWithDetails(@Param("userId") Long userId);


    /**
     * 특정 채팅방의 상대방 조회 (1:1 채팅용)
     *
     * @param roomId 채팅방 ID
     * @param myUserId 내 사용자 ID
     * @return 상대방 참여 정보
     */
    @Query("SELECT cp FROM ChatParticipant cp " +
            "JOIN FETCH cp.user u " +
            "WHERE cp.chatRoom.id = :roomId " +
            "AND u.id <> :myUserId")
    Optional<ChatParticipant> findPartnerInRoom(@Param("roomId") Long roomId,
                                                @Param("myUserId") Long myUserId);

    /**
     * 채팅방의 모든 참여자 조회
     *
     * @param roomId 채팅방 ID
     * @return 참여자 목록
     */
    @Query("SELECT cp FROM ChatParticipant cp " +
            "JOIN FETCH cp.user u " +
            "WHERE cp.chatRoom.id = :roomId")
    List<ChatParticipant> findAllByRoomId(@Param("roomId") Long roomId);

    /**
     * 채팅방 + 사용자로 참여 정보 조회
     *
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID
     * @return 참여 정보
     */
    Optional<ChatParticipant> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);

    /**
     * roomId(UUID) + 사용자로 참여 정보 조회
     *
     * @param roomId 채팅방 UUID
     * @param userId 사용자 ID
     * @return 참여 정보
     */
    @Query("SELECT cp FROM ChatParticipant cp " +
            "WHERE cp.chatRoom.roomId = :roomId " +
            "AND cp.user.id = :userId")
    Optional<ChatParticipant> findByRoomIdAndUserId(@Param("roomId") String roomId,
                                                    @Param("userId") Long userId);
}