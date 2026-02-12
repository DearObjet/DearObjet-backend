package app.dearobjet.backend.domain.chat.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 채팅방 타입
 */
@RequiredArgsConstructor
@Getter
public enum ChatRoomType {
    ONE_TO_ONE("1:1 채팅"),
    GROUP("그룹 채팅");

    private final String description;
}
