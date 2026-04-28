package app.dearobjet.backend.domain.contract;

import app.dearobjet.backend.domain.contract.dto.AdjustContractInventoryRequest;
import app.dearobjet.backend.domain.contract.dto.AdjustContractInventoryResponse;
import app.dearobjet.backend.domain.contract.dto.ContractApplicationCountResponse;
import app.dearobjet.backend.domain.contract.dto.ContractInboundConfirmResponse;
import app.dearobjet.backend.domain.contract.dto.ContractInventoryDetailResponse;
import app.dearobjet.backend.domain.contract.dto.ContractInventoryListResponse;
import app.dearobjet.backend.domain.contract.dto.ContractMemoResponse;
import app.dearobjet.backend.domain.contract.dto.UpdateContractMemoRequest;
import app.dearobjet.backend.domain.contract.entity.ContractProduct;
import app.dearobjet.backend.domain.contract.entity.ContractProductStockMovement;
import app.dearobjet.backend.domain.contract.entity.ShopArtistContract;
import app.dearobjet.backend.domain.contract.enums.ContractProductListingStatus;
import app.dearobjet.backend.domain.contract.enums.ContractProductStockMovementType;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import app.dearobjet.backend.domain.contract.repository.ContractProductRepository;
import app.dearobjet.backend.domain.contract.repository.ContractProductStockMovementRepository;
import app.dearobjet.backend.domain.shop.entity.Shop;
import app.dearobjet.backend.domain.user.repository.ShopRepository;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final ContractProductRepository contractProductRepository;
    private final ContractProductStockMovementRepository contractProductStockMovementRepository;
    private final ShopRepository shopRepository;

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

    private ShopArtistContract getApprovedContract(Long contractId, Long shopId) {
        return contractRepository.findApprovedByIdAndShopId(contractId, shopId, ContractStatus.APPROVED)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "계약된 작가를 찾을 수 없습니다."
                ));
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
