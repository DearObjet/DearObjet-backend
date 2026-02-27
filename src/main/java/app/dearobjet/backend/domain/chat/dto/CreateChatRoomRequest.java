package app.dearobjet.backend.domain.chat.dto;

import app.dearobjet.backend.domain.chat.entity.ChatRoomType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 채팅방 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateChatRoomRequest {

    @NotNull(message = "채팅방 타입은 필수입니다")
    private ChatRoomType type;

    @NotEmpty(message = "참여자 목록은 필수입니다")
    private List<Long> participantIds;

    /**
     * 1:1 채팅방 생성용 편의 생성자
     */
    public static CreateChatRoomRequest oneToOne(Long partnerId) {
        return new CreateChatRoomRequest(ChatRoomType.ONE_TO_ONE, List.of(partnerId));
    }
}
