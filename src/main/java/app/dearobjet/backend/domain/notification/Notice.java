package app.dearobjet.backend.domain.notification;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

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

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    @ColumnDefault("'PUBLISHED'")
    private NoticeStatus status = NoticeStatus.PUBLISHED;   // DRAFT | PUBLISHED | HIDDEN

    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean pinned = false;

    @Column(nullable = false)
    private OffsetDateTime publishedAt;

    public void update(NoticeUpdateRequest request) {
        if (request.getTarget() != null) this.target = request.getTarget();
        if (request.getCategory() != null) this.category = request.getCategory();
        if (request.getBadge() != null) this.badge = request.getBadge();
        if (request.getTitle() != null) this.title = request.getTitle();
        if (request.getBody() != null) this.body = request.getBody();
        if (request.getStatus() != null) this.status = request.getStatus();
        if (request.getPinned() != null) this.pinned = request.getPinned();
        if (request.getPublishedAt() != null) this.publishedAt = request.getPublishedAt();
    }
}
