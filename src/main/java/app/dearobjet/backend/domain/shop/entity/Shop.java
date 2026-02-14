package app.dearobjet.backend.domain.shop.entity;

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

    // 사업자 정보
    @Column(name = "business_number", nullable = false, unique = true)
    private String businessNumber;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "shop_name")
    private String shopName;

    @Column(name = "shop_description")
    private String shopDescription;

    private String address;
}