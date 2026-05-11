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
import app.dearobjet.backend.domain.contract.dto.ManagedArtistContractDetailResponse;
import app.dearobjet.backend.domain.contract.dto.ManagedArtistContractListResponse;
import app.dearobjet.backend.domain.contract.dto.ManagedShopContractActionResultResponse;
import app.dearobjet.backend.domain.contract.dto.ManagedShopContractDetailResponse;
import app.dearobjet.backend.domain.contract.dto.ManagedShopContractListResponse;
import app.dearobjet.backend.domain.contract.dto.SendContractRequest;
import app.dearobjet.backend.domain.contract.dto.SubmitArtistContractRequest;
import app.dearobjet.backend.domain.contract.dto.UpdateContractMemoRequest;
import app.dearobjet.backend.domain.contract.entity.ContractProduct;
import app.dearobjet.backend.domain.contract.entity.ContractProductStockMovement;
import app.dearobjet.backend.domain.contract.entity.ShopArtistContract;
import app.dearobjet.backend.domain.contract.enums.ContractProductListingStatus;
import app.dearobjet.backend.domain.contract.enums.ContractProductStockMovementType;
import app.dearobjet.backend.domain.contract.enums.ContractDocumentStatus;
import app.dearobjet.backend.domain.contract.enums.ContractRequestType;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import app.dearobjet.backend.domain.contract.repository.ContractProductRepository;
import app.dearobjet.backend.domain.contract.repository.ContractProductStockMovementRepository;
import app.dearobjet.backend.domain.shop.entity.Shop;
import app.dearobjet.backend.domain.artist.entity.Artist;
import app.dearobjet.backend.domain.contract.dto.projection.ArtistSuggestionRow;
import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.enums.UserStatus;
import app.dearobjet.backend.domain.user.repository.ArtistRepository;
import app.dearobjet.backend.domain.user.repository.ShopRepository;
import app.dearobjet.backend.global.exception.DuplicateEntityException;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.ErrorCode;
import app.dearobjet.backend.global.exception.InvalidInputException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ContractService {

    private static final int TERMINATION_GRACE_PERIOD_DAYS = 14;
    private static final int ARTIST_SUGGESTION_LIMIT = 10;
    private static final int ARTIST_ACCOUNT_SEARCH_LIMIT = 10;
    private static final int MIN_ARTIST_ACCOUNT_SEARCH_KEYWORD_LENGTH = 2;

    private static final List<ContractStatus> MANAGED_ARTIST_CONTRACT_STATUSES = List.of(
            ContractStatus.PENDING,
            ContractStatus.APPROVED,
            ContractStatus.ENDED
    );

    private static final List<ContractStatus> MANAGED_SHOP_CONTRACT_STATUSES = List.of(
            ContractStatus.PENDING,
            ContractStatus.APPROVED,
            ContractStatus.ENDED
    );

    private final ContractRepository contractRepository;
    private final ContractProductRepository contractProductRepository;
    private final ContractProductStockMovementRepository contractProductStockMovementRepository;
    private final ShopRepository shopRepository;
    private final ArtistRepository artistRepository;

    @Transactional(readOnly = true)
    public CompletedContractDocumentListResponse getCompletedContractDocuments(Long userId) {
        Shop shop = getShopByUserId(userId);

        return CompletedContractDocumentListResponse.from(
                contractRepository.findCompletedDocumentRowsByShopId(
                        shop.getShopId(),
                        ContractStatus.APPROVED,
                        ContractDocumentStatus.APPROVED
                )
        );
    }

    @Transactional
    public DeleteCompletedContractDocumentsResponse deleteCompletedContractDocuments(
            Long userId,
            DeleteCompletedContractDocumentsRequest request
    ) {
        Shop shop = getShopByUserId(userId);
        List<Long> requestedIds = distinctIds(request.getContractIds());
        List<ShopArtistContract> contracts = contractRepository.findAllByIdsAndShopId(
                requestedIds,
                shop.getShopId()
        );
        validateAllRequestedContractsFound(requestedIds, contracts);

        LocalDateTime deletedAt = LocalDateTime.now();
        contracts.forEach(contract -> {
            validateCompletedDocument(contract);
            contract.hideCompletedDocumentForShop(userId, deletedAt);
        });

        return DeleteCompletedContractDocumentsResponse.from(requestedIds);
    }

    @Transactional(readOnly = true)
    public ContractTemplateResponse getContractTemplate(Long userId) {
        return ContractTemplateResponse.from(getShopByUserId(userId));
    }

    @Transactional
    public ContractDocumentResponse sendContractToArtist(Long userId, Long artistId, SendContractRequest request) {
        Shop shop = getShopByUserId(userId);
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "계약서를 발송할 작가를 찾을 수 없습니다."
                ));
        validateContractTargetArtist(artist);

        if (contractRepository.existsPendingOrCurrentApprovedContract(
                shop.getShopId(),
                artist.getId(),
                ContractStatus.PENDING,
                ContractStatus.APPROVED,
                LocalDate.now()
        )) {
            throw new DuplicateEntityException(
                    ErrorCode.DUPLICATE_ENTITY,
                    "이미 진행 중이거나 유효한 계약이 있는 작가입니다."
            );
        }

        validateContractDates(request.getContractStartDate(), request.getContractEndDate());

        ShopArtistContract contract = ShopArtistContract.createPendingDocument(shop, artist);
        contract.fillShopContract(
                request.getShopBusinessName(),
                request.getShopOwnerName(),
                request.getShopBusinessNumber(),
                request.getShopAddress(),
                request.getShopContact(),
                request.getContractStartDate(),
                request.getContractEndDate(),
                request.getCommissionRate(),
                request.getSettlementDay(),
                request.getPaymentDay(),
                request.getContractDate(),
                request.getShopSignatureBusinessName(),
                request.getShopSignatureOwnerName()
        );

        return ContractDocumentResponse.from(contractRepository.save(contract));
    }

    @Transactional
    public ContractDocumentResponse submitArtistContract(
            Long userId,
            Long contractId,
            SubmitArtistContractRequest request
    ) {
        ShopArtistContract contract = getArtistOwnedContract(userId, contractId);
        if (contract.getContractStatus() != ContractStatus.PENDING
                || contract.getContractDocumentStatus() != ContractDocumentStatus.SHOP_SENT) {
            throw new InvalidInputException(
                    ErrorCode.INVALID_INPUT,
                    "작가 작성이 가능한 계약서가 아닙니다."
            );
        }

        contract.fillArtistContract(
                request.getArtistName(),
                normalizeOptional(request.getArtistBusinessNumber()),
                request.getArtistAddress(),
                request.getArtistContact(),
                request.getArtistBankName(),
                request.getArtistAccountHolder(),
                request.getArtistAccountNumber(),
                request.getArtistSignatureName()
        );

        return ContractDocumentResponse.from(contract);
    }

    @Transactional
    public ContractDocumentResponse approveContract(Long userId, Long contractId) {
        Shop shop = getShopByUserId(userId);
        ShopArtistContract contract = contractRepository.findManagedArtistContractByIdAndShopId(
                        contractId,
                        shop.getShopId(),
                        MANAGED_ARTIST_CONTRACT_STATUSES
                )
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "승인할 계약서 정보를 찾을 수 없습니다."
                ));

        if (contract.getContractStatus() != ContractStatus.PENDING
                || contract.getContractDocumentStatus() != ContractDocumentStatus.ARTIST_SUBMITTED) {
            throw new InvalidInputException(
                    ErrorCode.INVALID_INPUT,
                    "작가 작성 완료 후에만 계약 승인할 수 있습니다."
            );
        }

        contract.approveDocument();
        return ContractDocumentResponse.from(contract);
    }

    @Transactional(readOnly = true)
    public ContractApplicationCountResponse getPendingApplicationCount(Long userId) {
        getShopByUserId(userId);

        long applicationCount = contractRepository.countByShopUserIdAndContractStatus(userId, ContractStatus.PENDING);
        List<String> artistNames = contractRepository.findPendingApplicationArtistNamesByShopUserId(
                userId,
                ContractStatus.PENDING
        );
        return new ContractApplicationCountResponse(applicationCount, artistNames);
    }

    @Transactional(readOnly = true)
    public ManagedArtistContractListResponse getManagedArtists(Long userId) {
        Shop shop = getShopByUserId(userId);

        return ManagedArtistContractListResponse.from(
                contractRepository.findManagedArtistRowsByShopId(
                        shop.getShopId(),
                        MANAGED_ARTIST_CONTRACT_STATUSES,
                        ContractStatus.PENDING,
                        ContractStatus.APPROVED,
                        ContractStatus.ENDED
                ),
                LocalDate.now()
        );
    }

    @Transactional(readOnly = true)
    public ArtistSuggestionListResponse getArtistSuggestions(Long userId) {
        Shop shop = getShopByUserId(userId);
        List<ArtistSuggestionRow> rows = new ArrayList<>(contractRepository.findArtistSuggestionRows(
                shop.getShopId(),
                Role.ARTIST,
                UserStatus.ACTIVE,
                ContractStatus.PENDING,
                ContractStatus.APPROVED,
                LocalDate.now()
        ));

        Collections.shuffle(rows);
        return ArtistSuggestionListResponse.from(
                rows.stream()
                        .limit(ARTIST_SUGGESTION_LIMIT)
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public ArtistAccountSearchResponse searchArtistAccounts(Long userId, String keyword) {
        Shop shop = getShopByUserId(userId);
        String normalizedKeyword = normalizeSearchKeyword(keyword);
        if (normalizedKeyword.length() < MIN_ARTIST_ACCOUNT_SEARCH_KEYWORD_LENGTH) {
            return ArtistAccountSearchResponse.from(List.of());
        }

        return ArtistAccountSearchResponse.from(
                contractRepository.searchArtistAccountRows(
                        shop.getShopId(),
                        Role.ARTIST,
                        UserStatus.ACTIVE,
                        ContractStatus.PENDING,
                        ContractStatus.APPROVED,
                        LocalDate.now(),
                        "%" + normalizedKeyword + "%",
                        PageRequest.of(0, ARTIST_ACCOUNT_SEARCH_LIMIT)
                )
        );
    }

    @Transactional(readOnly = true)
    public ManagedArtistContractDetailResponse getManagedArtistContract(Long userId, Long contractId) {
        Shop shop = getShopByUserId(userId);

        ShopArtistContract contract = contractRepository.findManagedArtistContractByIdAndShopId(
                        contractId,
                        shop.getShopId(),
                        MANAGED_ARTIST_CONTRACT_STATUSES
                )
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "계약서 정보를 찾을 수 없습니다."
                ));

        return ManagedArtistContractDetailResponse.from(contract);
    }

    @Transactional(readOnly = true)
    public ManagedShopContractListResponse getManagedShops(Long userId) {
        Artist artist = getArtistByUserId(userId);
        LocalDate today = LocalDate.now();

        return ManagedShopContractListResponse.from(
                contractRepository.findManagedShopRowsByArtistId(
                        artist.getId(),
                        MANAGED_SHOP_CONTRACT_STATUSES,
                        ContractStatus.TERMINATED,
                        LocalDateTime.now().minusDays(TERMINATION_GRACE_PERIOD_DAYS),
                        ContractStatus.PENDING,
                        ContractStatus.APPROVED,
                        ContractStatus.ENDED
                ),
                today
        );
    }

    @Transactional(readOnly = true)
    public ManagedShopContractDetailResponse getManagedShopContract(Long userId, Long contractId) {
        Artist artist = getArtistByUserId(userId);

        ShopArtistContract contract = contractRepository.findManagedShopContractByIdAndArtistId(
                        contractId,
                        artist.getId(),
                        MANAGED_SHOP_CONTRACT_STATUSES,
                        ContractStatus.TERMINATED,
                        LocalDateTime.now().minusDays(TERMINATION_GRACE_PERIOD_DAYS)
                )
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "계약서 정보를 찾을 수 없습니다."
                ));

        return ManagedShopContractDetailResponse.from(contract);
    }

    @Transactional
    public ManagedShopContractActionResultResponse requestManagedShopContractExtension(
            Long userId,
            Long contractId
    ) {
        ShopArtistContract contract = getArtistOwnedContract(userId, contractId);
        if (!isArtistRenewable(contract, LocalDate.now())) {
            throw new InvalidInputException(
                    ErrorCode.INVALID_INPUT,
                    "계약연장 신청이 가능한 계약이 아닙니다."
            );
        }

        contract.requestExtension();
        return ManagedShopContractActionResultResponse.from(contract);
    }

    @Transactional
    public ManagedShopContractActionResultResponse requestManagedShopContractRelease(
            Long userId,
            Long contractId
    ) {
        ShopArtistContract contract = getArtistOwnedContract(userId, contractId);
        if (!isArtistRenewable(contract, LocalDate.now())) {
            throw new InvalidInputException(
                    ErrorCode.INVALID_INPUT,
                    "해제신청이 가능한 계약이 아닙니다."
            );
        }

        contract.requestRelease();
        return ManagedShopContractActionResultResponse.from(contract);
    }

    @Transactional
    public ManagedShopContractActionResultResponse cancelManagedShopContractRelease(
            Long userId,
            Long contractId
    ) {
        ShopArtistContract contract = getArtistOwnedContract(userId, contractId);
        if (contract.getContractStatus() != ContractStatus.PENDING
                || contract.getContractRequestType() != ContractRequestType.RELEASE) {
            throw new InvalidInputException(
                    ErrorCode.INVALID_INPUT,
                    "해제취소가 가능한 계약이 아닙니다."
            );
        }

        contract.cancelReleaseRequest();
        return ManagedShopContractActionResultResponse.from(contract);
    }

    @Transactional
    public ContractTerminationResponse terminateManagedArtistContract(Long userId, Long contractId) {
        Shop shop = getShopByUserId(userId);

        ShopArtistContract contract = contractRepository.findManagedArtistContractByIdAndShopId(
                        contractId,
                        shop.getShopId(),
                        MANAGED_ARTIST_CONTRACT_STATUSES
                )
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "해지할 계약 정보를 찾을 수 없습니다."
                ));

        if (!canTerminate(contract, LocalDate.now())) {
            throw new InvalidInputException(
                    ErrorCode.INVALID_INPUT,
                    "해지 승인 또는 계약해지가 가능한 계약이 아닙니다."
            );
        }

        contract.terminate(LocalDateTime.now());
        return ContractTerminationResponse.from(contract);
    }

    @Transactional(readOnly = true)
    public ContractInventoryListResponse getInventory(Long userId) {
        Shop shop = getShopByUserId(userId);

        return ContractInventoryListResponse.from(
                contractRepository.findInventoryRowsByShopId(
                        shop.getShopId(),
                        ContractStatus.APPROVED,
                        ContractProductListingStatus.ACTIVE
                )
        );
    }

    @Transactional(readOnly = true)
    public ContractInventoryDetailResponse getInventoryProducts(Long userId, Long contractId) {
        Shop shop = getShopByUserId(userId);
        ShopArtistContract contract = getApprovedContract(contractId, shop.getShopId());

        return ContractInventoryDetailResponse.from(
                contract,
                contractProductRepository.findInventoryProductRowsByContractId(
                        contractId,
                        shop.getShopId(),
                        ContractStatus.APPROVED,
                        ContractProductListingStatus.ACTIVE,
                        ContractProductStockMovementType.INBOUND
                )
        );
    }

    @Transactional
    public ContractMemoResponse updateInventoryMemo(
            Long userId,
            Long contractId,
            UpdateContractMemoRequest request
    ) {
        Shop shop = getShopByUserId(userId);
        ShopArtistContract contract = getApprovedContract(contractId, shop.getShopId());
        contract.updateMemo(request.getMemo());

        return ContractMemoResponse.from(contract);
    }

    @Transactional
    public ContractInboundConfirmResponse confirmRecentInbound(Long userId, Long contractId) {
        Shop shop = getShopByUserId(userId);
        ShopArtistContract contract = getApprovedContract(contractId, shop.getShopId());
        contract.confirmRecentInbound(userId, LocalDateTime.now());

        return ContractInboundConfirmResponse.from(contract);
    }

    @Transactional
    public AdjustContractInventoryResponse adjustInventory(
            Long userId,
            Long contractProductId,
            AdjustContractInventoryRequest request
    ) {
        Shop shop = getShopByUserId(userId);
        ContractProduct contractProduct = contractProductRepository.findOwnedInventoryById(
                        contractProductId,
                        shop.getShopId(),
                        ContractStatus.APPROVED,
                        ContractProductListingStatus.ACTIVE
                )
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "재고 품목을 찾을 수 없습니다."
                ));

        ContractProductStockMovement movement = applyMovement(userId, contractProduct, request);
        if (movement.getMovementType() == ContractProductStockMovementType.INBOUND) {
            contractProduct.getShopArtistContract().markRecentInboundPending();
        }
        contractProductStockMovementRepository.save(movement);

        return AdjustContractInventoryResponse.from(contractProduct, movement);
    }

    private Shop getShopByUserId(Long userId) {
        return shopRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found for user: " + userId));
    }

    private Artist getArtistByUserId(Long userId) {
        return artistRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Artist not found for user: " + userId));
    }

    private ShopArtistContract getArtistOwnedContract(Long userId, Long contractId) {
        Artist artist = getArtistByUserId(userId);

        return contractRepository.findContractByIdAndArtistId(contractId, artist.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "계약서 정보를 찾을 수 없습니다."
                ));
    }

    private ShopArtistContract getApprovedContract(Long contractId, Long shopId) {
        return contractRepository.findApprovedByIdAndShopId(contractId, shopId, ContractStatus.APPROVED)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "계약된 작가를 찾을 수 없습니다."
                ));
    }

    private boolean canTerminate(ShopArtistContract contract, LocalDate today) {
        if (contract.getContractStatus() == ContractStatus.PENDING) {
            return contract.getContractRequestType() == ContractRequestType.RELEASE;
        }
        if (contract.getContractStatus() == ContractStatus.ENDED) {
            return true;
        }
        if (contract.getContractStatus() != ContractStatus.APPROVED) {
            return false;
        }

        LocalDate endDate = contract.getContractEndDate();
        return endDate != null && !today.isBefore(endDate.plusDays(TERMINATION_GRACE_PERIOD_DAYS));
    }

    private boolean isArtistRenewable(ShopArtistContract contract, LocalDate today) {
        if (contract.getContractStatus() == ContractStatus.ENDED) {
            return true;
        }
        if (contract.getContractStatus() != ContractStatus.APPROVED) {
            return false;
        }

        LocalDate endDate = contract.getContractEndDate();
        return endDate != null && today != null && today.isAfter(endDate);
    }

    private String normalizeSearchKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }
        return keyword.trim().toLowerCase();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private void validateContractDates(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new InvalidInputException(
                    ErrorCode.INVALID_INPUT,
                    "계약 종료일은 계약 시작일보다 빠를 수 없습니다."
            );
        }
    }

    private void validateContractTargetArtist(Artist artist) {
        if (artist.getUser() == null
                || artist.getUser().getRole() != Role.ARTIST
                || artist.getUser().getUserStatus() != UserStatus.ACTIVE) {
            throw new InvalidInputException(
                    ErrorCode.INVALID_INPUT,
                    "계약서를 발송할 수 있는 활성 작가가 아닙니다."
            );
        }
    }

    private List<Long> distinctIds(List<Long> ids) {
        return ids.stream()
                .distinct()
                .toList();
    }

    private void validateAllRequestedContractsFound(
            List<Long> requestedIds,
            List<ShopArtistContract> contracts
    ) {
        Set<Long> foundIds = new HashSet<>(
                contracts.stream()
                        .map(ShopArtistContract::getShopArtistContractsId)
                        .toList()
        );
        boolean allFound = requestedIds.stream().allMatch(foundIds::contains);
        if (!allFound) {
            throw new EntityNotFoundException(
                    ErrorCode.ENTITY_NOT_FOUND,
                    "삭제할 계약서 중 소품샵 소유가 아니거나 존재하지 않는 계약서가 있습니다."
            );
        }
    }

    private void validateCompletedDocument(ShopArtistContract contract) {
        if (contract.getContractStatus() != ContractStatus.APPROVED
                || contract.getContractDocumentStatus() != ContractDocumentStatus.APPROVED) {
            throw new InvalidInputException(
                    ErrorCode.INVALID_INPUT,
                    "완료된 계약서만 삭제할 수 있습니다."
            );
        }
    }

    private ContractProductStockMovement applyMovement(
            Long userId,
            ContractProduct contractProduct,
            AdjustContractInventoryRequest request
    ) {
        ContractProductStockMovementType movementType = request.getMovementType();
        int quantity = request.getQuantity();

        return switch (movementType) {
            case INBOUND -> contractProduct.applyInbound(
                    quantity,
                    request.getOccurredAt(),
                    userId,
                    request.getMemo()
            );
            case ADJUSTMENT_INCREASE -> contractProduct.applyAdjustmentIncrease(
                    quantity,
                    request.getOccurredAt(),
                    userId,
                    request.getMemo()
            );
            case ADJUSTMENT_DECREASE -> contractProduct.applyAdjustmentDecrease(
                    quantity,
                    request.getOccurredAt(),
                    userId,
                    request.getMemo()
            );
            case SALE_DECREASE -> contractProduct.applySaleDecrease(
                    quantity,
                    request.getOccurredAt(),
                    userId,
                    request.getMemo()
            );
            case RETURN_INCREASE -> contractProduct.applyReturnIncrease(
                    quantity,
                    request.getOccurredAt(),
                    userId,
                    request.getMemo()
            );
            case CANCEL_RESTORE -> contractProduct.applyCancelRestore(
                    quantity,
                    request.getOccurredAt(),
                    userId,
                    request.getMemo()
            );
        };
    }
}
