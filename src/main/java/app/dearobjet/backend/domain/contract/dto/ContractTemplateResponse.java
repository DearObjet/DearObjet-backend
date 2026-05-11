package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.shop.entity.Shop;
import app.dearobjet.backend.domain.user.entity.BusinessProfile;
import app.dearobjet.backend.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContractTemplateResponse {

    private final String shopBusinessName;
    private final String shopOwnerName;
    private final String shopBusinessNumber;
    private final String shopAddress;
    private final String shopContact;
    private final String shopSignatureBusinessName;
    private final String shopSignatureOwnerName;

    private final String artistName;
    private final String artistBusinessNumber;
    private final String artistAddress;
    private final String artistContact;
    private final String artistBankName;
    private final String artistAccountHolder;
    private final String artistAccountNumber;
    private final String artistSignatureName;

    public static ContractTemplateResponse from(Shop shop) {
        BusinessProfile profile = shop.getBusinessProfile();
        User user = shop.getUser();
        String businessName = valueOrEmpty(profile.getBusinessName());
        String ownerName = valueOrEmpty(profile.getOwnerName());

        return new ContractTemplateResponse(
                businessName,
                ownerName,
                valueOrEmpty(profile.getBusinessNumber()),
                valueOrEmpty(profile.getBusinessAddress()),
                firstNonBlank(profile.getBusinessPhoneNumber(), user.getPhoneNumber()),
                businessName,
                ownerName,
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                ""
        );
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return valueOrEmpty(second);
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
