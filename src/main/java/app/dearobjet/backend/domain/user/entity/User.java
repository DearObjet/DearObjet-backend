package app.dearobjet.backend.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "sms_agreement")
    private Boolean smsAgreement;

    @Column(name = "marketing_agreement")
    private Boolean marketingAgreement;

    @Column(nullable = false)
    private String role;  // CUSTOMER, ARTIST, SHOP

    @Column(name = "profile_url")
    private String profileUrl;

    @Column(name = "user_status")
    private String userStatus;


    @Column(name = "login_type")
    private String loginType;  // EMAIL, KAKAO, NAVER, GOOGLE

    @Column(name = "social_id")
    private String socialId;

    // 비즈니스 메서드
    public void updateProfile(String name, String profileUrl) {
        this.name = name;
        this.profileUrl = profileUrl;
    }

    public void deactivate() {
        this.userStatus = "INACTIVE";
    }

}
