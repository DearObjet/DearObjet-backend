package app.dearobjet.backend.domain.artist.entity;

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
    private Long artistsId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "business_number")
    private String businessNumber;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "portfolio_url")
    private String portfolioUrl;

    @Column(name = "field6")
    private String field6;

    // 비즈니스 메서드
    public void updateProfile(String businessName, String bio, String portfolioUrl) {
        this.businessName = businessName;
        this.bio = bio;
        this.portfolioUrl = portfolioUrl;
    }
}