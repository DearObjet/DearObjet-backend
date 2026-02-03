package app.dearobjet.backend.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateChatRoomResponse {
    private Long chatRoomId;
    private boolean isNew; // true: 새로 생성됨, false: 이미 존재하던 방
}