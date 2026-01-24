package app.dearobjet.backend.domain.user.entity;

import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.enums.UserStatus;
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

    @Column(unique = true)
    private String email;

    private String name;

    @Column(unique = true)
    private String phoneNumber;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "sms_agreement")
    private Boolean smsAgreement;

    @Column(name = "marketing_agreement")
    private Boolean marketingAgreement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "profile_url")
    private String profileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false)
    private UserStatus userStatus;

    // 카카오 단일
//    @Column(name = "login_type")
//    private String loginType;  // EMAIL, KAKAO, NAVER, GOOGLE

    @Column(name = "social_id")
    private String socialId;

    // 비즈니스 메서드
    public void updateProfile(String name, String profileUrl) {
        this.name = name;
        this.profileUrl = profileUrl;
    }

    public void deactivate() {
        this.userStatus = UserStatus.INACTIVE;
    }

    public void completeRegistration(
            String name,
            String email,
            String phoneNumber,
            Boolean smsAgreement,
            Boolean marketingAgreement,
            Role role
    ) {
        if (this.userStatus != UserStatus.PENDING) {
            throw new IllegalStateException("User already registered");
        }

        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.smsAgreement = smsAgreement;
        this.marketingAgreement = marketingAgreement;
        this.role = role;
        this.userStatus = UserStatus.ACTIVE;
    }
}
