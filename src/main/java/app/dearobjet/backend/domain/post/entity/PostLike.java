package app.dearobjet.backend.domain.post.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "post_likes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_likes_id")
    private Long postLikesId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "field")
    private String field;

    @Column(name = "field2")
    private String field2;

    @Column(name = "field3")
    private String field3;

    @Column(name = "field4")
    private String field4;

    @Column(name = "field5")
    private String field5;
}