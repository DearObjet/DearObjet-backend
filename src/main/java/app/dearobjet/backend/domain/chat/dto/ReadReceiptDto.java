package app.dearobjet.backend.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 읽음 확인 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadReceiptDto {

    private String roomId;
    private Long userId;
    private LocalDateTime readAt;

    public static ReadReceiptDto of(String roomId, Long userId, LocalDateTime readAt) {
        return ReadReceiptDto.builder()
                .roomId(roomId)
                .userId(userId)
                .readAt(readAt)
                .build();
    }
}
