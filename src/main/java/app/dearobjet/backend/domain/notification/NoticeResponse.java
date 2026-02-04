package app.dearobjet.backend.domain.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
public class NoticeResponse {
    private final Long noticeId;
    private final NoticeCategory category;
    private final String title;
    private final String body;
    private final OffsetDateTime publishedAt;
}
