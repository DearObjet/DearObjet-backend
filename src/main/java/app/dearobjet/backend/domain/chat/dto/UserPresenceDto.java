package app.dearobjet.backend.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 온라인 상태 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPresenceDto {

    public enum Status {
        ONLINE,
        AWAY,
        OFFLINE
    }

    private Long userId;
    private Status status;
    private LocalDateTime lastSeenAt;
    private String activeRoomId;

    public static UserPresenceDto online(Long userId, String activeRoomId) {
        return UserPresenceDto.builder()
                .userId(userId)
                .status(Status.ONLINE)
                .lastSeenAt(LocalDateTime.now())
                .activeRoomId(activeRoomId)
                .build();
    }

    public static UserPresenceDto offline(Long userId) {
        return UserPresenceDto.builder()
                .userId(userId)
                .status(Status.OFFLINE)
                .lastSeenAt(LocalDateTime.now())
                .build();
    }
}
