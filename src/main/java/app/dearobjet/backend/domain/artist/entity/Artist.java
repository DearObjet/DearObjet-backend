package app.dearobjet.backend.domain.artist.entity;

import app.dearobjet.backend.domain.user.entity.BusinessProfile;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "artists")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Artist extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "artists_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_profile_id", nullable = false, unique = true)
    private BusinessProfile businessProfile;

    @Column(name = "instagram_id")
    private String instagramId;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "portfolio_url")
    private String portfolioUrl;

    // 비즈니스 메서드
    public void updateProfile(String businessName, String bio, String portfolioUrl) {
        if (businessName != null && businessProfile != null) {
            businessProfile.changeBusinessName(businessName);
        }
        this.bio = bio;
        this.portfolioUrl = portfolioUrl;
    }

    public void changeInstagramId(String instagramId) {
        this.instagramId = instagramId;
    }
}
