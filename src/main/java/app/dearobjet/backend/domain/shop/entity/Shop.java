package app.dearobjet.backend.domain.shop.entity;

import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.enums.BusinessCategory;
import app.dearobjet.backend.domain.user.enums.BusinessType;
import app.dearobjet.backend.domain.user.enums.Specialty;
import app.dearobjet.backend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shops")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Shop extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shop_id")
    private Long shopId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // 사업자 정보
    @Column(name = "business_number", nullable = false, unique = true)
    private String businessNumber;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "business_adress")
    private String businessAddress;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "business_license_url")
    private String businessLicenseUrl;

    @Column(name = "shop_name")
    private String shopName;

    @Column(name = "shop_description")
    private String shopDescription;

    @Column(name = "business_hours")
    private String businessHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", nullable = false)
    private BusinessType businessType;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_category", nullable = false)
    private BusinessCategory businessCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "specialty", nullable = false)
    private Specialty specialty;

    @Column(name="review_data_agreement", nullable=false)
    private Boolean reviewDataAgreement = false;

    public void updateCoordinates(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
