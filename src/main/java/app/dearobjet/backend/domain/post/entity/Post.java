package app.dearobjet.backend.domain.post.entity;

import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Builder.Default
    @Column(name = "blinded", nullable = false, columnDefinition = "boolean default false")
    private Boolean blinded = false;

    public void update(String content, String imageUrls, Boolean isPublic) {
        this.content = content;
        if (imageUrls != null) {
            this.imageUrls = imageUrls;
        }
        if (isPublic != null) {
            this.isPublic = isPublic;
        }
    }

    public void changeBlinded(boolean blinded) {
        this.blinded = blinded;
    }
}