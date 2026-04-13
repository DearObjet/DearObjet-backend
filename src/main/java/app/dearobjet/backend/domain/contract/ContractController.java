package app.dearobjet.backend.domain.contract;

import app.dearobjet.backend.domain.contract.dto.ContractApplicationCountResponse;
import app.dearobjet.backend.global.api.ApiResponse;
import app.dearobjet.backend.global.auth.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contracts")
public class ContractController {

    private final ContractService contractService;

    @GetMapping("/pending-count")
    public ApiResponse<ContractApplicationCountResponse> getPendingApplicationCount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.of(contractService.getPendingApplicationCount(userDetails.getUserId()));
    }
}
