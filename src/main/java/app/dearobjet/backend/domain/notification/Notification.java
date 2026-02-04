package app.dearobjet.backend.domain.notification;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "field3")
    private String field3;
}
