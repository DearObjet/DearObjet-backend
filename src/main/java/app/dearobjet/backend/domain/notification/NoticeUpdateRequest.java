package app.dearobjet.backend.domain.notification;

import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
public class NoticeUpdateRequest {
    private NoticeTarget target;
    private NoticeCategory category;
    private String badge;
    private String title;
    private String body;
    private NoticeStatus status;
    private Boolean pinned;
    private OffsetDateTime publishedAt;
}
