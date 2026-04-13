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
    private boolean typing;

    public static TypingIndicatorDto of(String roomId, Long userId, String userName, boolean typing) {
        return TypingIndicatorDto.builder()
                .roomId(roomId)
                .userId(userId)
                .userName(userName)
                .typing(typing)
                .build();
    }
}
