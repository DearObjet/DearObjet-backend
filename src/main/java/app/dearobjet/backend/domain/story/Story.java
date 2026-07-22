package app.dearobjet.backend.domain.story;

import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Story extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "story_id")
    private Long storyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "thumbnail_image_url", nullable = false)
    private String thumbnailImageUrl;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @Column(name = "blinded", nullable = false, columnDefinition = "boolean default false")
    private Boolean blinded = false;

    public void changeBlinded(boolean blinded) {
        this.blinded = blinded;
    }

    public void update(String title, String content, String thumbnailImageUrl) {
        this.title = title;
        this.content = content;
        if (thumbnailImageUrl != null && !thumbnailImageUrl.isBlank()) {
            this.thumbnailImageUrl = thumbnailImageUrl;
        }
    }
}
