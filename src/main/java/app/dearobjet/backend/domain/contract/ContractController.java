package app.dearobjet.backend.domain.contract;

import app.dearobjet.backend.domain.contract.dto.AdjustContractInventoryRequest;
import app.dearobjet.backend.domain.contract.dto.AdjustContractInventoryResponse;
import app.dearobjet.backend.domain.contract.dto.ContractApplicationCountResponse;
import app.dearobjet.backend.domain.contract.dto.ContractInboundConfirmResponse;
import app.dearobjet.backend.domain.contract.dto.ContractInventoryDetailResponse;
import app.dearobjet.backend.domain.contract.dto.ContractInventoryListResponse;
import app.dearobjet.backend.domain.contract.dto.ContractMemoResponse;
import app.dearobjet.backend.domain.contract.dto.ManagedArtistContractDetailResponse;
import app.dearobjet.backend.domain.contract.dto.ManagedArtistContractListResponse;
import app.dearobjet.backend.domain.contract.dto.UpdateContractMemoRequest;
import app.dearobjet.backend.global.api.ApiResponse;
import app.dearobjet.backend.global.auth.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contracts")
public class ContractController {

    private final ContractService contractService;

    @GetMapping("/pending-count")
    public ApiResponse<ContractApplicationCountResponse> getPendingApplicationCount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId
    ) {
        return ApiResponse.of(contractService.getPendingApplicationCount(resolveUserId(userDetails, userId)));
    }

    @GetMapping("/artists")
    public ApiResponse<ManagedArtistContractListResponse> getManagedArtists(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId
    ) {
        return ApiResponse.of(contractService.getManagedArtists(resolveUserId(userDetails, userId)));
    }

    @GetMapping("/artists/{contractId}")
    public ApiResponse<ManagedArtistContractDetailResponse> getManagedArtistContract(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @PathVariable Long contractId
    ) {
        return ApiResponse.of(
                contractService.getManagedArtistContract(
                        resolveUserId(userDetails, userId),
                        contractId
                )
        );
    }

    @GetMapping("/inventory")
    public ApiResponse<ContractInventoryListResponse> getInventory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId
    ) {
        return ApiResponse.of(contractService.getInventory(resolveUserId(userDetails, userId)));
    }

    @GetMapping("/inventory/{contractId}/products")
    public ApiResponse<ContractInventoryDetailResponse> getInventoryProducts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @PathVariable Long contractId
    ) {
        return ApiResponse.of(
                contractService.getInventoryProducts(
                        resolveUserId(userDetails, userId),
                        contractId
                )
        );
    }

    @PatchMapping("/inventory/{contractId}/memo")
    public ApiResponse<ContractMemoResponse> updateInventoryMemo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @PathVariable Long contractId,
            @Valid @RequestBody UpdateContractMemoRequest request
    ) {
        return ApiResponse.of(
                contractService.updateInventoryMemo(
                        resolveUserId(userDetails, userId),
                        contractId,
                        request
                )
        );
    }

    @PatchMapping("/inventory/{contractId}/inbound-confirmation")
    public ApiResponse<ContractInboundConfirmResponse> confirmRecentInbound(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @PathVariable Long contractId
    ) {
        return ApiResponse.of(
                contractService.confirmRecentInbound(
                        resolveUserId(userDetails, userId),
                        contractId
                )
        );
    }

    @PostMapping("/inventory/{contractProductId}/stock-movements")
    public ApiResponse<AdjustContractInventoryResponse> adjustInventory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @PathVariable Long contractProductId,
            @Valid @RequestBody AdjustContractInventoryRequest request
    ) {
        return ApiResponse.of(
                contractService.adjustInventory(
                        resolveUserId(userDetails, userId),
                        contractProductId,
                        request
                )
        );
    }

    private Long resolveUserId(CustomUserDetails userDetails, Long userId) {
        if (userDetails != null) {
            return userDetails.getUserId();
        }
        if (userId != null) {
            return userId;
        }
        throw new IllegalArgumentException("인증 정보가 없으면 userId 쿼리 파라미터가 필요합니다.");
    }
}
