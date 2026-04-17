package app.dearobjet.backend.domain.user.dto;

import app.dearobjet.backend.domain.user.entity.BusinessProfile;
import app.dearobjet.backend.domain.user.entity.User;

public record BusinessProfileDetailResponse(
        String profileUrl,
        String phoneNumber,
        String email,
        String instagramId,
        String businessName,
        String businessNumber,
        String ownerName,
        String businessAddress,
        String bankName,
        String bankAccountNumber,
        String accountHolder,
        String bankbookImageUrl,
        Boolean isBankbookVerified,
        String taxInvoiceEmail,
        String hometaxApiKey
) {
    public static BusinessProfileDetailResponse from(
            User user,
            BusinessProfile businessProfile,
            String instagramId
    ) {
        return new BusinessProfileDetailResponse(
                user.getProfileUrl(),
                user.getPhoneNumber(),
                user.getEmail(),
                instagramId,
                businessProfile.getBusinessName(),
                businessProfile.getBusinessNumber(),
                businessProfile.getOwnerName(),
                businessProfile.getBusinessAddress(),
                businessProfile.getBankName(),
                businessProfile.getBankAccountNumber(),
                businessProfile.getAccountHolder(),
                businessProfile.getBankbookImageUrl(),
                businessProfile.getIsBankbookVerified(),
                businessProfile.getTaxInvoiceEmail(),
                businessProfile.getHometaxApiKey()
        );
    }
}
