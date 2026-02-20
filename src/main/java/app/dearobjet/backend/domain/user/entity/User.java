package app.dearobjet.backend.domain.user.entity;

import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.enums.UserStatus;
import app.dearobjet.backend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(unique = true)
    private String email;

    private String name;

    @Column(name= "phone_number", unique = true)
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

    @Column(name = "social_id")
    private String socialId;

    public void deactivate() {
        this.userStatus = UserStatus.INACTIVE;
    }

    public void completeRegistration(
            String name,
            String phoneNumber,
            Boolean smsAgreement,
            Boolean marketingAgreement
    ) {
        if (this.role != Role.TEMP) {
            throw new IllegalStateException("User already registered");
        }

        this.name = name;
        this.phoneNumber = phoneNumber;
        this.smsAgreement = smsAgreement;
        this.marketingAgreement = marketingAgreement;
    }
    public void changeRole(Role role) {
        this.role = role;
    }

    public void changePhone(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
