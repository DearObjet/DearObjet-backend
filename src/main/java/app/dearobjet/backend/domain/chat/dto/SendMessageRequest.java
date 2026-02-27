package app.dearobjet.backend.domain.chat.dto;

import app.dearobjet.backend.domain.chat.entity.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 메시지 전송 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    @NotBlank(message = "채팅방 ID는 필수입니다")
    private String roomId;

    @NotBlank(message = "메시지 내용은 필수입니다")
    @Size(max = 5000, message = "메시지는 5000자를 초과할 수 없습니다")
    private String content;

    @NotNull(message = "메시지 타입은 필수입니다")
    private MessageType messageType;

    /**
     * 텍스트 메시지용 편의 생성자
     */
    public static SendMessageRequest ofText(String roomId, String content) {
        return new SendMessageRequest(roomId, content, MessageType.TEXT);
    }
}
