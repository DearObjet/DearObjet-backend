package app.dearobjet.backend.domain.notification;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NoticeTarget target;   // USER | ARTIST_SHOP

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoticeCategory category;   // IMPORTANT | GENERAL | FESTIVAL | CULTURE_PERFORMANCE | EVENT

    @Column(length = 30)
    private String badge;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(nullable = false)
    private OffsetDateTime publishedAt;

    public void update(NoticeUpdateRequest request) {
        if (request.getTarget() != null) this.target = request.getTarget();
        if (request.getCategory() != null) this.category = request.getCategory();
        if (request.getBadge() != null) this.badge = request.getBadge();
        if (request.getTitle() != null) this.title = request.getTitle();
        if (request.getBody() != null) this.body = request.getBody();
        if (request.getPublishedAt() != null) this.publishedAt = request.getPublishedAt();
    }
}
