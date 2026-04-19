package app.dearobjet.backend.domain.user.entity;

import app.dearobjet.backend.domain.user.enums.BusinessCategory;
import app.dearobjet.backend.domain.user.enums.BusinessType;
import app.dearobjet.backend.domain.user.enums.Specialty;
import app.dearobjet.backend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "business_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BusinessProfile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", nullable = false)
    private BusinessType businessType;

    @Column(name = "business_number", nullable = false)
    private String businessNumber;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "business_address", nullable = false)
    private String businessAddress;

    @Column(name = "business_license_url")
    private String businessLicenseUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_category", nullable = false)
    private BusinessCategory businessCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "specialty", nullable = false)
    private Specialty specialty;

    @Column(name = "review_data_agreement", nullable = false)
    private Boolean reviewDataAgreement;

    @Column(name = "business_phone_number")
    private String businessPhoneNumber;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "account_holder")
    private String accountHolder;

    @Column(name = "bankbook_image_url")
    private String bankbookImageUrl;

    @Builder.Default
    @Column(name = "is_bankbook_verified", nullable = false)
    private Boolean isBankbookVerified = false;

    @Column(name = "tax_invoice_email")
    private String taxInvoiceEmail;

    @Column(name = "hometax_api_key")
    private String hometaxApiKey;

    public void changeBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public void changeBusinessPhoneNumber(String businessPhoneNumber) {
        this.businessPhoneNumber = businessPhoneNumber;
    }

    public void changeBankName(String bankName) {
        this.bankName = bankName;
    }

    public void changeBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public void changeAccountHolder(String accountHolder) {
        this.accountHolder = accountHolder;
    }

    public void changeBankbookImageUrl(String bankbookImageUrl) {
        this.bankbookImageUrl = bankbookImageUrl;
    }

    public void changeTaxInvoiceEmail(String taxInvoiceEmail) {
        this.taxInvoiceEmail = taxInvoiceEmail;
    }

    public void changeBankbookVerified(Boolean isBankbookVerified) {
        this.isBankbookVerified = isBankbookVerified;
    }
}
