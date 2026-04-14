package app.dearobjet.backend.domain.shop.entity;

import app.dearobjet.backend.domain.user.entity.BusinessProfile;
import app.dearobjet.backend.domain.user.entity.User;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_profile_id", nullable = false, unique = true)
    private BusinessProfile businessProfile;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "instagram_id")
    private String instagramId;

    @Column(name = "shop_name")
    private String shopName;

    @Column(name = "shop_description")
    private String shopDescription;

    public void updateCoordinates(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getShopName() {
        return businessProfile == null ? null : businessProfile.getBusinessName();
    }

    public String getBusinessName() {
        return businessProfile == null ? null : businessProfile.getBusinessName();
    }

    public String getBusinessAddress() {
        return businessProfile == null ? null : businessProfile.getBusinessAddress();
    }

    public void changeInstagramId(String instagramId) {
        this.instagramId = instagramId;
    }
}
