package app.dearobjet.backend.domain.contract.entity;

import app.dearobjet.backend.domain.artist.entity.Artist;
import app.dearobjet.backend.domain.contract.enums.CommissionType;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import app.dearobjet.backend.domain.shop.entity.Shop;
import app.dearobjet.backend.global.common.entity.BaseTimeEntity;
import app.dearobjet.backend.global.exception.ErrorCode;
import app.dearobjet.backend.global.exception.InvalidInputException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 샵-작가 사이의 입점 계약 엔티티
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

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "contract_status", nullable = false, length = 20)
    private ContractStatus contractStatus = ContractStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "commission_type", length = 20)
    private CommissionType commissionType;

    @Column(name = "commission_value", precision = 19, scale = 4)
    private BigDecimal commissionValue;

    @Column(name = "contract_start_date")
    private LocalDate contractStartDate;

    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;

    @Builder.Default
    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo = "";

    @Builder.Default
    @Column(name = "recent_inbound_confirmed")
    private Boolean recentInboundConfirmed = false;

    @Column(name = "recent_inbound_confirmed_at")
    private LocalDateTime recentInboundConfirmedAt;

    @Column(name = "recent_inbound_confirmed_by_user_id")
    private Long recentInboundConfirmedByUserId;

    public void updateMemo(String memo) {
        this.memo = memo == null ? "" : memo;
    }

    public void markRecentInboundPending() {
        this.recentInboundConfirmed = false;
        this.recentInboundConfirmedAt = null;
        this.recentInboundConfirmedByUserId = null;
    }

    public void confirmRecentInbound(Long confirmedByUserId, LocalDateTime confirmedAt) {
        if (confirmedByUserId == null) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "입고확인 사용자 ID는 필수입니다.");
        }
        if (confirmedAt == null) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "입고확인 시각은 필수입니다.");
        }

        this.recentInboundConfirmed = true;
        this.recentInboundConfirmedAt = confirmedAt;
        this.recentInboundConfirmedByUserId = confirmedByUserId;
    }
}
