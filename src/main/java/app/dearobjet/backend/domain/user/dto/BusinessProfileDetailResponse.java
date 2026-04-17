package app.dearobjet.backend.domain.user.dto;

import app.dearobjet.backend.domain.user.entity.BusinessProfile;
import app.dearobjet.backend.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessProfileDetailResponse {

    private String profileUrl;
    private String userName;
    private String phoneNumber;
    private String email;
    private String businessPhoneNumber;
    private String instagramId;

    private String businessName;
    private String businessNumber;
    private String ownerName;
    private String businessAddress;

    private String bankName;
    private String bankAccountNumber;
    private String accountHolder;
    private String bankbookImageUrl;
    private Boolean isBankbookVerified;

    private String taxInvoiceEmail;
    private String hometaxApiKey;

    public static BusinessProfileDetailResponse from(
            User user,
            BusinessProfile businessProfile,
            String instagramId
    ) {
        return BusinessProfileDetailResponse.builder()
                .profileUrl(user.getProfileUrl())
                .userName(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .businessPhoneNumber(businessProfile.getBusinessPhoneNumber())
                .instagramId(instagramId)
                .businessName(businessProfile.getBusinessName())
                .businessNumber(businessProfile.getBusinessNumber())
                .ownerName(businessProfile.getOwnerName())
                .businessAddress(businessProfile.getBusinessAddress())
                .bankName(businessProfile.getBankName())
                .bankAccountNumber(businessProfile.getBankAccountNumber())
                .accountHolder(businessProfile.getAccountHolder())
                .bankbookImageUrl(businessProfile.getBankbookImageUrl())
                .isBankbookVerified(businessProfile.getIsBankbookVerified())
                .taxInvoiceEmail(businessProfile.getTaxInvoiceEmail())
                .hometaxApiKey(businessProfile.getHometaxApiKey())
                .build();
    }
}
