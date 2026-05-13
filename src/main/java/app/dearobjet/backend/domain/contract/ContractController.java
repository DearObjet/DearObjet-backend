package app.dearobjet.backend.domain.contract;

import app.dearobjet.backend.domain.contract.dto.AdjustContractInventoryRequest;
import app.dearobjet.backend.domain.contract.dto.AdjustContractInventoryResponse;
import app.dearobjet.backend.domain.contract.dto.ArtistAccountSearchResponse;
import app.dearobjet.backend.domain.contract.dto.ArtistSuggestionListResponse;
import app.dearobjet.backend.domain.contract.dto.CompletedContractDocumentListResponse;
import app.dearobjet.backend.domain.contract.dto.ContractApplicationCountResponse;
import app.dearobjet.backend.domain.contract.dto.ContractDocumentResponse;
import app.dearobjet.backend.domain.contract.dto.ContractInboundConfirmResponse;
import app.dearobjet.backend.domain.contract.dto.ContractInventoryDetailResponse;
import app.dearobjet.backend.domain.contract.dto.ContractInventoryListResponse;
import app.dearobjet.backend.domain.contract.dto.ContractMemoResponse;
import app.dearobjet.backend.domain.contract.dto.ContractTemplateResponse;
import app.dearobjet.backend.domain.contract.dto.ContractTerminationResponse;
import app.dearobjet.backend.domain.contract.dto.DeleteCompletedContractDocumentsRequest;
import app.dearobjet.backend.domain.contract.dto.DeleteCompletedContractDocumentsResponse;
import app.dearobjet.backend.domain.contract.dto.InProgressContractDocumentListResponse;
import app.dearobjet.backend.domain.contract.dto.ManagedArtistContractDetailResponse;
import app.dearobjet.backend.domain.contract.dto.ManagedArtistContractListResponse;
import app.dearobjet.backend.domain.contract.dto.ManagedShopContractActionResultResponse;
import app.dearobjet.backend.domain.contract.dto.ManagedShopContractDetailResponse;
import app.dearobjet.backend.domain.contract.dto.ManagedShopContractListResponse;
import app.dearobjet.backend.domain.contract.dto.SendContractRequest;
import app.dearobjet.backend.domain.contract.dto.ShopSuggestionListResponse;
import app.dearobjet.backend.domain.contract.dto.SubmitArtistContractRequest;
import app.dearobjet.backend.domain.contract.dto.UpdateContractMemoRequest;
import app.dearobjet.backend.global.api.ApiResponse;
import app.dearobjet.backend.global.auth.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    @GetMapping("/documents/completed")
    public ApiResponse<CompletedContractDocumentListResponse> getCompletedContractDocuments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId
    ) {
        return ApiResponse.of(
                contractService.getCompletedContractDocuments(resolveUserId(userDetails, userId))
        );
    }

    @GetMapping("/documents/in-progress")
    public ApiResponse<InProgressContractDocumentListResponse> getInProgressContractDocuments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId
    ) {
        return ApiResponse.of(
                contractService.getInProgressContractDocuments(resolveUserId(userDetails, userId))
        );
    }

    @DeleteMapping("/documents/completed")
    public ApiResponse<DeleteCompletedContractDocumentsResponse> deleteCompletedContractDocuments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @Valid @RequestBody DeleteCompletedContractDocumentsRequest request
    ) {
        return ApiResponse.of(
                contractService.deleteCompletedContractDocuments(
                        resolveUserId(userDetails, userId),
                        request
                )
        );
    }

    @GetMapping("/template")
    public ApiResponse<ContractTemplateResponse> getContractTemplate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId
    ) {
        return ApiResponse.of(contractService.getContractTemplate(resolveUserId(userDetails, userId)));
    }

    @PostMapping("/artists/{artistId}/send")
    public ApiResponse<ContractDocumentResponse> sendContractToArtist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @PathVariable Long artistId,
            @Valid @RequestBody SendContractRequest request
    ) {
        return ApiResponse.of(
                contractService.sendContractToArtist(
                        resolveUserId(userDetails, userId),
                        artistId,
                        request
                )
        );
    }

    @PatchMapping("/shops/{contractId}/artist-submission")
    public ApiResponse<ContractDocumentResponse> submitArtistContract(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @PathVariable Long contractId,
            @Valid @RequestBody SubmitArtistContractRequest request
    ) {
        return ApiResponse.of(
                contractService.submitArtistContract(
                        resolveUserId(userDetails, userId),
                        contractId,
                        request
                )
        );
    }

    @PatchMapping("/artists/{contractId}/approval")
    public ApiResponse<ContractDocumentResponse> approveContract(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @PathVariable Long contractId
    ) {
        return ApiResponse.of(
                contractService.approveContract(
                        resolveUserId(userDetails, userId),
                        contractId
                )
        );
    }

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

    @GetMapping("/shops")
    public ApiResponse<ManagedShopContractListResponse> getManagedShops(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId
    ) {
        return ApiResponse.of(contractService.getManagedShops(resolveUserId(userDetails, userId)));
    }

    @GetMapping("/artists/suggestions")
    public ApiResponse<ArtistSuggestionListResponse> getArtistSuggestions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId
    ) {
        return ApiResponse.of(contractService.getArtistSuggestions(resolveUserId(userDetails, userId)));
    }

    @GetMapping("/shops/suggestions")
    public ApiResponse<ShopSuggestionListResponse> getShopSuggestions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId
    ) {
        return ApiResponse.of(contractService.getShopSuggestions(resolveUserId(userDetails, userId)));
    }

    @GetMapping("/artists/search")
    public ApiResponse<ArtistAccountSearchResponse> searchArtistAccounts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @RequestParam String keyword
    ) {
        return ApiResponse.of(contractService.searchArtistAccounts(resolveUserId(userDetails, userId), keyword));
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

    @GetMapping("/shops/{contractId}")
    public ApiResponse<ManagedShopContractDetailResponse> getManagedShopContract(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @PathVariable Long contractId
    ) {
        return ApiResponse.of(
                contractService.getManagedShopContract(
                        resolveUserId(userDetails, userId),
                        contractId
                )
        );
    }

    @PatchMapping("/shops/{contractId}/extension-request")
    public ApiResponse<ManagedShopContractActionResultResponse> requestManagedShopContractExtension(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @PathVariable Long contractId
    ) {
        return ApiResponse.of(
                contractService.requestManagedShopContractExtension(
                        resolveUserId(userDetails, userId),
                        contractId
                )
        );
    }

    @PatchMapping("/shops/{contractId}/release-request")
    public ApiResponse<ManagedShopContractActionResultResponse> requestManagedShopContractRelease(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @PathVariable Long contractId
    ) {
        return ApiResponse.of(
                contractService.requestManagedShopContractRelease(
                        resolveUserId(userDetails, userId),
                        contractId
                )
        );
    }

    @PatchMapping("/shops/{contractId}/release-cancellation")
    public ApiResponse<ManagedShopContractActionResultResponse> cancelManagedShopContractRelease(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @PathVariable Long contractId
    ) {
        return ApiResponse.of(
                contractService.cancelManagedShopContractRelease(
                        resolveUserId(userDetails, userId),
                        contractId
                )
        );
    }

    @PatchMapping("/artists/{contractId}/termination")
    public ApiResponse<ContractTerminationResponse> terminateManagedArtistContract(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @PathVariable Long contractId
    ) {
        return ApiResponse.of(
                contractService.terminateManagedArtistContract(
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
