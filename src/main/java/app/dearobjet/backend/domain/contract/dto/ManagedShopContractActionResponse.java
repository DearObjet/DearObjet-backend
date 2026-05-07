package app.dearobjet.backend.domain.contract.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ManagedShopContractActionResponse {

    private final String code;
    private final String label;
    private final Boolean enabled;

    public static ManagedShopContractActionResponse of(String code, String label, boolean enabled) {
        return new ManagedShopContractActionResponse(code, label, enabled);
    }
}
