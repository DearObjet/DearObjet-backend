package app.dearobjet.backend.domain.chat.dto;

import app.dearobjet.backend.domain.chat.entity.ChatRoom;
import app.dearobjet.backend.domain.user.entity.User;

import java.time.LocalDateTime;

public record ChatRoomListResponse(
        Long chatRoomId,
        String lastMessage,
        LocalDateTime lastModifiedAt,
        Integer unreadCount,

        // 상대방 정보
        Long partnerId,
        String partnerNickname,
        String partnerProfileImage
) {
    public static ChatRoomListResponse of(ChatRoom chatRoom, User partner, Integer unreadCount) {
        return new ChatRoomListResponse(
                chatRoom.getId(),
                chatRoom.getLastMessage(),
                chatRoom.getLastModifiedAt(),
                unreadCount,
                partner.getUserId(),
                partner.getName(),
                partner.getProfileImage()
        );
    }
}