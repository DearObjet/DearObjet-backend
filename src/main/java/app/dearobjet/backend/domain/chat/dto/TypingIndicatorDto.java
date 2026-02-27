package app.dearobjet.backend.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 타이핑 상태 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypingIndicatorDto {

    private String roomId;
    private Long userId;
    private String userName;
    private boolean isTyping;

    public static TypingIndicatorDto of(String roomId, Long userId, String userName, boolean isTyping) {
        return TypingIndicatorDto.builder()
                .roomId(roomId)
                .userId(userId)
                .userName(userName)
                .isTyping(isTyping)
                .build();
    }
}
