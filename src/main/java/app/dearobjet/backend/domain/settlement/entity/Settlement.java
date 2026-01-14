package app.dearobjet.backend.domain.settlement.entity;

import app.dearobjet.backend.domain.contract.entity.ShopArtistContract;
import jakarta.persistence.*;
import lombok.*;

/**
 * 정산 엔티티
 */
@Entity
@Table(name = "settlement")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlement_id")
    private Long settlementId;

    @Column(name = "commission_rate")
    private Double commissionRate;

    @Column(name = "commission_price")
    private Double commissionPrice;

    @Column(name = "artist_amount")
    private Double artistAmount;

    @Column(name = "shop_amount")
    private Double shopAmount;

    @Column(name = "settlement_status")
    private String settlementStatus;

    @Column(name = "settlement_period")
    private String settlementPeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_artist_contracts_id")
    private ShopArtistContract shopArtistContract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_id")
    private Sale sale;
}