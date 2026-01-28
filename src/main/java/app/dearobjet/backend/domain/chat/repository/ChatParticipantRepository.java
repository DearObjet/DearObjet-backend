package app.dearobjet.backend.domain.chat.repository;

import app.dearobjet.backend.domain.chat.entity.ChatParticipant;
import app.dearobjet.backend.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    /**
     * 내 참여 정보 조회 (채팅방 정보 Fetch Join)
     */
    @Query("SELECT cp FROM ChatParticipant cp " +
            "JOIN FETCH cp.chatRoom cr " +
            "WHERE cp.user.userId = :userId " +
            "ORDER BY cr.lastModifiedAt DESC")
    List<ChatParticipant> findAllMyself(@Param("userId") Long userId);

    /**
     * 파트너 정보 조회 (유저 정보 Fetch Join)
     * 주어진 채팅방 ID 목록(roomIds)에 포함되면서, 내가 아닌(userId != myId) 참여자 조회
     */
    @Query("SELECT cp FROM ChatParticipant cp " +
            "JOIN FETCH cp.user u " +
            "WHERE cp.chatRoom.id IN :roomIds " +
            "AND cp.user.userId <> :myUserId")
    List<ChatParticipant> findAllPartners(@Param("roomIds") List<Long> roomIds,
                                          @Param("myUserId") Long myUserId);

    /**
     * 두 유저(user1, user2)가 공통으로 참여하고 있는 채팅방이 있는지 조회
     * Self Join을 사용하여 하나의 방 ID에 두 유저가 모두 속해있는지 확인
     */
    @Query("SELECT p1.chatRoom " +
            "FROM ChatParticipant p1 " +
            "JOIN ChatParticipant p2 ON p1.chatRoom.id = p2.chatRoom.id " +
            "WHERE p1.user.userId = :firstUserId " +
            "AND p2.user.userId = :secondUserId")
    Optional<ChatRoom> findExistingChatRoom(@Param("firstUserId") Long firstUserId,
                                            @Param("secondUserId") Long secondUserId);
}