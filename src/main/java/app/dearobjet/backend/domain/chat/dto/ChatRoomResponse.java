package app.dearobjet.backend.domain.chat.dto;

import app.dearobjet.backend.domain.chat.entity.ChatParticipant;
import app.dearobjet.backend.domain.chat.entity.ChatRoom;
import app.dearobjet.backend.domain.chat.entity.ChatRoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 채팅방 정보 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomResponse {

    private Long id;
    private String roomId;
    private ChatRoomType type;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private int unreadCount;
    private List<ParticipantResponse> participants;

    // 1:1 채팅의 경우 상대방 정보
    private String partnerName;
    private String partnerProfileImage;

    /**
     * 엔티티로부터 DTO 생성
     */
    public static ChatRoomResponse from(ChatRoom chatRoom, ChatParticipant myParticipation, Long currentUserId) {
        List<ParticipantResponse> participantResponses = chatRoom.getParticipants().stream()
                .map(ParticipantResponse::from)
                .toList();

        // 1:1 채팅인 경우 상대방 정보 추출
        String partnerName = null;
        String partnerProfileImage = null;

        if (chatRoom.getType() == ChatRoomType.ONE_TO_ONE) {
            ChatParticipant partner = chatRoom.getParticipants().stream()
                    .filter(p -> !p.getUser().getId().equals(currentUserId))
                    .findFirst()
                    .orElse(null);

            if (partner != null) {
                partnerName = partner.getUser().getName();
                partnerProfileImage = partner.getUser().getProfileImage();
            }
        }

        return ChatRoomResponse.builder()
                .id(chatRoom.getId())
                .roomId(chatRoom.getRoomId())
                .type(chatRoom.getType())
                .lastMessage(chatRoom.getLastMessage())
                .lastMessageAt(chatRoom.getUpdatedAt())
                .unreadCount(myParticipation != null ? myParticipation.getUnreadCount() : 0)
                .participants(participantResponses)
                .partnerName(partnerName)
                .partnerProfileImage(partnerProfileImage)
                .build();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParticipantResponse {
        private Long userId;
        private String nickname;
        private String profileImageUrl;
        private LocalDateTime joinedAt;
        private LocalDateTime lastReadAt;

        public static ParticipantResponse from(ChatParticipant participant) {
            return ParticipantResponse.builder()
                    .userId(participant.getUser().getId())
                    .nickname(participant.getUser().getName())
                    .profileImageUrl(participant.getUser().getProfileImage())
                    .joinedAt(participant.getJoinedAt())
                    .lastReadAt(participant.getLastReadAt())
                    .build();
        }
    }
}
