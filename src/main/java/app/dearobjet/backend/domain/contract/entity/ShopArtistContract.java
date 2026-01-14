package app.dearobjet.backend.domain.contract.entity;

import app.dearobjet.backend.domain.artist.entity.Artist;
import app.dearobjet.backend.domain.shop.entity.Shop;
import app.dearobjet.backend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 샵-작가 계약 엔티티
 */
@Entity
@Table(name = "shop_artist_contracts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ShopArtistContract extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shop_artist_contracts_id")
    private Long shopArtistContractsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artists_id")
    private Artist artist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @Column(name = "contract_status")
    private String contractStatus;

    @Column(name = "applied_at")
    private java.time.LocalDateTime appliedAt;

    @Column(name = "approved_at")
    private java.time.LocalDateTime approvedAt;

    @Column(name = "ended_at")
    private java.time.LocalDateTime endedAt;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "last_modified_at")
    private java.time.LocalDateTime lastModifiedAt;

    @Column(name = "commission_type")
    private String commissionType;

    @Column(name = "commission_value")
    private Double commissionValue;

    @Column(name = "contract_url")
    private String contractUrl;
}