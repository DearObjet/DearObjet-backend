package app.dearobjet.backend.domain.contract.entity;

import app.dearobjet.backend.domain.artist.entity.Artist;
import app.dearobjet.backend.domain.contract.enums.CommissionType;
import app.dearobjet.backend.domain.contract.enums.ContractDocumentStatus;
import app.dearobjet.backend.domain.contract.enums.ContractRequestType;
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
    @Enumerated(EnumType.STRING)
    @Column(name = "contract_document_status", nullable = false, length = 20)
    private ContractDocumentStatus contractDocumentStatus = ContractDocumentStatus.NONE;

    @Column(name = "shop_contract_business_name", length = 100)
    private String shopContractBusinessName;

    @Column(name = "shop_contract_owner_name", length = 50)
    private String shopContractOwnerName;

    @Column(name = "shop_contract_business_number", length = 30)
    private String shopContractBusinessNumber;

    @Column(name = "shop_contract_address", length = 500)
    private String shopContractAddress;

    @Column(name = "shop_contract_contact", length = 30)
    private String shopContractContact;

    @Column(name = "settlement_day")
    private Integer settlementDay;

    @Column(name = "payment_day")
    private Integer paymentDay;

    @Column(name = "contract_date")
    private LocalDate contractDate;

    @Column(name = "shop_signature_business_name", length = 100)
    private String shopSignatureBusinessName;

    @Column(name = "shop_signature_owner_name", length = 50)
    private String shopSignatureOwnerName;

    @Column(name = "artist_contract_name", length = 100)
    private String artistContractName;

    @Column(name = "artist_contract_business_number", length = 30)
    private String artistContractBusinessNumber;

    @Column(name = "artist_contract_address", length = 500)
    private String artistContractAddress;

    @Column(name = "artist_contract_contact", length = 30)
    private String artistContractContact;

    @Column(name = "artist_bank_name", length = 50)
    private String artistBankName;

    @Column(name = "artist_account_holder", length = 50)
    private String artistAccountHolder;

    @Column(name = "artist_account_number", length = 50)
    private String artistAccountNumber;

    @Column(name = "artist_signature_name", length = 100)
    private String artistSignatureName;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "contract_request_type", nullable = false, length = 20)
    private ContractRequestType contractRequestType = ContractRequestType.NONE;

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

    @Column(name = "terminated_at")
    private LocalDateTime terminatedAt;

    @Column(name = "shop_document_deleted_at")
    private LocalDateTime shopDocumentDeletedAt;

    @Column(name = "shop_document_deleted_by_user_id")
    private Long shopDocumentDeletedByUserId;

    public static ShopArtistContract createPendingDocument(Shop shop, Artist artist) {
        return ShopArtistContract.builder()
                .shop(shop)
                .artist(artist)
                .contractStatus(ContractStatus.PENDING)
                .contractRequestType(ContractRequestType.NONE)
                .contractDocumentStatus(ContractDocumentStatus.SHOP_SENT)
                .commissionType(CommissionType.RATE)
                .build();
    }

    public void fillShopContract(
            String businessName,
            String ownerName,
            String businessNumber,
            String address,
            String contact,
            LocalDate contractStartDate,
            LocalDate contractEndDate,
            BigDecimal commissionRate,
            Integer settlementDay,
            Integer paymentDay,
            LocalDate contractDate,
            String signatureBusinessName,
            String signatureOwnerName
    ) {
        this.shopContractBusinessName = businessName;
        this.shopContractOwnerName = ownerName;
        this.shopContractBusinessNumber = businessNumber;
        this.shopContractAddress = address;
        this.shopContractContact = contact;
        this.contractStartDate = contractStartDate;
        this.contractEndDate = contractEndDate;
        this.commissionType = CommissionType.RATE;
        this.commissionValue = commissionRate;
        this.settlementDay = settlementDay;
        this.paymentDay = paymentDay;
        this.contractDate = contractDate;
        this.shopSignatureBusinessName = signatureBusinessName;
        this.shopSignatureOwnerName = signatureOwnerName;
        this.contractDocumentStatus = ContractDocumentStatus.SHOP_SENT;
    }

    public void fillArtistContract(
            String artistName,
            String businessNumber,
            String address,
            String contact,
            String bankName,
            String accountHolder,
            String accountNumber,
            String signatureName
    ) {
        this.artistContractName = artistName;
        this.artistContractBusinessNumber = businessNumber;
        this.artistContractAddress = address;
        this.artistContractContact = contact;
        this.artistBankName = bankName;
        this.artistAccountHolder = accountHolder;
        this.artistAccountNumber = accountNumber;
        this.artistSignatureName = signatureName;
        this.contractDocumentStatus = ContractDocumentStatus.ARTIST_SUBMITTED;
    }

    public void approveDocument() {
        this.contractStatus = ContractStatus.APPROVED;
        this.contractRequestType = ContractRequestType.NONE;
        this.contractDocumentStatus = ContractDocumentStatus.APPROVED;
    }

    public void hideCompletedDocumentForShop(Long deletedByUserId, LocalDateTime deletedAt) {
        if (deletedByUserId == null) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "계약서 삭제 사용자 ID는 필수입니다.");
        }
        if (deletedAt == null) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "계약서 삭제 시각은 필수입니다.");
        }

        this.shopDocumentDeletedAt = deletedAt;
        this.shopDocumentDeletedByUserId = deletedByUserId;
    }

    public void updateMemo(String memo) {
        this.memo = memo == null ? "" : memo;
    }

    public void markRecentInboundPending() {
        this.recentInboundConfirmed = false;
        this.recentInboundConfirmedAt = null;
        this.recentInboundConfirmedByUserId = null;
    }

    public void terminate(LocalDateTime terminatedAt) {
        this.contractStatus = ContractStatus.TERMINATED;
        this.contractRequestType = ContractRequestType.NONE;
        this.terminatedAt = terminatedAt;
    }

    public void terminate() {
        terminate(LocalDateTime.now());
    }

    public void requestExtension() {
        this.contractStatus = ContractStatus.PENDING;
        this.contractRequestType = ContractRequestType.EXTENSION;
    }

    public void requestRelease() {
        this.contractStatus = ContractStatus.PENDING;
        this.contractRequestType = ContractRequestType.RELEASE;
    }

    public void cancelReleaseRequest() {
        this.contractStatus = ContractStatus.ENDED;
        this.contractRequestType = ContractRequestType.NONE;
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
