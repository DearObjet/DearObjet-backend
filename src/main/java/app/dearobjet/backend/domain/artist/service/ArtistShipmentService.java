package app.dearobjet.backend.domain.artist.service;

import app.dearobjet.backend.domain.artist.dto.ArtistShipmentAvailableProductListResponse;
import app.dearobjet.backend.domain.artist.dto.ArtistShipmentProductListResponse;
import app.dearobjet.backend.domain.artist.dto.ArtistShipmentShopListResponse;
import app.dearobjet.backend.domain.artist.dto.CreateArtistShipmentItemResponse;
import app.dearobjet.backend.domain.artist.dto.CreateArtistShipmentRequest;
import app.dearobjet.backend.domain.artist.dto.CreateArtistShipmentResponse;
import app.dearobjet.backend.domain.artist.entity.Artist;
import app.dearobjet.backend.domain.contract.ContractRepository;
import app.dearobjet.backend.domain.contract.entity.ContractProduct;
import app.dearobjet.backend.domain.contract.entity.ContractProductStockMovement;
import app.dearobjet.backend.domain.contract.entity.ShopArtistContract;
import app.dearobjet.backend.domain.contract.enums.CommissionType;
import app.dearobjet.backend.domain.contract.enums.ContractProductListingStatus;
import app.dearobjet.backend.domain.contract.enums.ContractProductStockMovementType;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import app.dearobjet.backend.domain.contract.repository.ContractProductRepository;
import app.dearobjet.backend.domain.contract.repository.ContractProductStockMovementRepository;
import app.dearobjet.backend.domain.shop.entity.Product;
import app.dearobjet.backend.domain.shop.enums.ProductStatus;
import app.dearobjet.backend.domain.shop.repository.ProductRepository;
import app.dearobjet.backend.domain.user.repository.ArtistRepository;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.ErrorCode;
import app.dearobjet.backend.global.exception.InvalidInputException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArtistShipmentService {

    private static final List<ContractStatus> SHIPMENT_PRODUCT_CONTRACT_STATUSES = List.of(
            ContractStatus.APPROVED,
            ContractStatus.ENDED,
            ContractStatus.TERMINATED
    );
    private static final int FIRST_PAGE = 1;
    private static final int FIRST_PAGE_INDEX = 0;
    private static final int MIN_PAGE_SIZE = 1;
    private static final BigDecimal PERCENT_DENOMINATOR = new BigDecimal("100");
    private static final int MONEY_SCALE = 2;
    private static final String SHIPMENT_INBOUND_MEMO = "작가 출고";

    private final ArtistRepository artistRepository;
    private final ContractRepository contractRepository;
    private final ContractProductRepository contractProductRepository;
    private final ContractProductStockMovementRepository contractProductStockMovementRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public ArtistShipmentShopListResponse getShipmentShops(Long userId) {
        Artist artist = getArtistByUserId(userId);

        return ArtistShipmentShopListResponse.from(
                contractRepository.findShipmentShopRowsByArtistId(
                        artist.getId(),
                        ContractStatus.APPROVED,
                        ContractProductListingStatus.ACTIVE,
                        LocalDate.now()
                )
        );
    }

    @Transactional(readOnly = true)
    public ArtistShipmentProductListResponse getShipmentProducts(Long userId, Long shopId) {
        Artist artist = getArtistByUserId(userId);
        List<ShopArtistContract> contracts = getShipmentProductContracts(artist.getId(), shopId);
        List<Long> contractIds = contracts.stream()
                .map(ShopArtistContract::getShopArtistContractsId)
                .toList();

        return ArtistShipmentProductListResponse.from(
                shopId,
                contracts,
                contractProductRepository.findShipmentProductRowsByContractIds(
                        contractIds,
                        artist.getId(),
                        ContractProductStockMovementType.INBOUND
                )
        );
    }

    @Transactional(readOnly = true)
    public ArtistShipmentProductListResponse getRecentShipmentProducts(Long userId, Long shopId) {
        Artist artist = getArtistByUserId(userId);
        ShopArtistContract recentContract = getRecentShipmentProductContract(artist.getId(), shopId);
        List<ShopArtistContract> contracts = List.of(recentContract);

        return ArtistShipmentProductListResponse.from(
                shopId,
                contracts,
                contractProductRepository.findShipmentProductRowsByContractIds(
                        List.of(recentContract.getShopArtistContractsId()),
                        artist.getId(),
                        ContractProductStockMovementType.INBOUND
                )
        );
    }

    @Transactional(readOnly = true)
    public ArtistShipmentAvailableProductListResponse getAvailableProducts(
            Long userId,
            Long shopId,
            String keyword,
            int page,
            int size
    ) {
        validatePageSize(size);
        Artist artist = getArtistByUserId(userId);
        ShopArtistContract contract = getCurrentApprovedContract(artist.getId(), shopId);
        PageRequest pageRequest = PageRequest.of(
                toPageIndex(page),
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "productsId"))
        );

        Page<Product> products = productRepository.findShipmentAvailableProducts(
                artist.getId(),
                ProductStatus.ACTIVE,
                normalizeSearchKeyword(keyword),
                pageRequest
        );

        return ArtistShipmentAvailableProductListResponse.from(
                shopId,
                contract.getShopArtistContractsId(),
                products,
                page
        );
    }

    @Transactional
    public CreateArtistShipmentResponse createShipment(
            Long userId,
            Long shopId,
            CreateArtistShipmentRequest request
    ) {
        Artist artist = getArtistByUserId(userId);
        ShopArtistContract contract = getCurrentApprovedContract(artist.getId(), shopId);
        validateDuplicateProductIds(request);

        List<Long> productIds = request.getItems().stream()
                .map(CreateArtistShipmentRequest.Item::getProductId)
                .toList();
        Map<Long, Product> productMap = getOwnedActiveProductMap(productIds, artist.getId());
        Map<Long, ContractProduct> contractProductMap = getActiveContractProductMap(
                contract.getShopArtistContractsId(),
                productIds
        );

        List<CreateArtistShipmentItemResponse> items = request.getItems().stream()
                .map(item -> shipProduct(userId, contract, productMap.get(item.getProductId()), contractProductMap, item))
                .toList();

        contract.markRecentInboundPending();

        return CreateArtistShipmentResponse.from(
                shopId,
                contract.getShopArtistContractsId(),
                items
        );
    }

    private Artist getArtistByUserId(Long userId) {
        return artistRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "작가 정보를 찾을 수 없습니다."
                ));
    }

    private List<ShopArtistContract> getShipmentProductContracts(Long artistId, Long shopId) {
        List<ShopArtistContract> contracts = contractRepository.findShipmentProductContractsByArtistIdAndShopId(
                        artistId,
                        shopId,
                        SHIPMENT_PRODUCT_CONTRACT_STATUSES
                );
        if (contracts.isEmpty()) {
            throw new EntityNotFoundException(
                    ErrorCode.ENTITY_NOT_FOUND,
                    "계약서 정보를 찾을 수 없습니다."
            );
        }
        return contracts;
    }

    private ShopArtistContract getRecentShipmentProductContract(Long artistId, Long shopId) {
        return getShipmentProductContracts(artistId, shopId).get(0);
    }

    private ShopArtistContract getCurrentApprovedContract(Long artistId, Long shopId) {
        return contractRepository.findCurrentApprovedContractsByArtistIdAndShopId(
                        artistId,
                        shopId,
                        ContractStatus.APPROVED,
                        LocalDate.now()
                ).stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "현재 승인된 계약 정보를 찾을 수 없습니다."
                ));
    }

    private Map<Long, Product> getOwnedActiveProductMap(List<Long> productIds, Long artistId) {
        Map<Long, Product> productMap = productRepository.findByProductsIdInAndArtistIdAndStatus(
                        productIds,
                        artistId,
                        ProductStatus.ACTIVE
                ).stream()
                .collect(Collectors.toMap(Product::getProductsId, Function.identity()));

        if (productMap.size() != productIds.size()) {
            throw new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "상품을 찾을 수 없습니다.");
        }
        return productMap;
    }

    private Map<Long, ContractProduct> getActiveContractProductMap(Long contractId, List<Long> productIds) {
        return contractProductRepository.findActiveProductsByContractIdAndProductIds(
                        contractId,
                        productIds,
                        ContractProductListingStatus.ACTIVE
                ).stream()
                .collect(Collectors.toMap(
                        contractProduct -> contractProduct.getProduct().getProductsId(),
                        Function.identity()
                ));
    }

    private CreateArtistShipmentItemResponse shipProduct(
            Long userId,
            ShopArtistContract contract,
            Product product,
            Map<Long, ContractProduct> contractProductMap,
            CreateArtistShipmentRequest.Item item
    ) {
        int quantity = item.getQuantity();
        product.decreaseStockQuantity(quantity, item.getVersion());

        ContractProduct contractProduct = contractProductMap.computeIfAbsent(
                product.getProductsId(),
                productId -> contractProductRepository.save(createContractProduct(contract, product))
        );
        ContractProductStockMovement movement = contractProduct.applyInbound(
                quantity,
                LocalDateTime.now(),
                userId,
                SHIPMENT_INBOUND_MEMO
        );
        contractProductStockMovementRepository.save(movement);

        return CreateArtistShipmentItemResponse.from(product, contractProduct, quantity);
    }

    private ContractProduct createContractProduct(ShopArtistContract contract, Product product) {
        SettlementAmounts settlementAmounts = calculateSettlementAmounts(
                product.getPrice(),
                contract.getCommissionType(),
                contract.getCommissionValue()
        );
        return ContractProduct.create(
                contract,
                product,
                product.getPrice(),
                settlementAmounts.marginAmount(),
                settlementAmounts.unitSettlementAmount()
        );
    }

    private SettlementAmounts calculateSettlementAmounts(
            BigDecimal sellingPrice,
            CommissionType commissionType,
            BigDecimal commissionValue
    ) {
        if (sellingPrice == null || commissionType == null || commissionValue == null) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "정산 정보를 계산할 수 없습니다.");
        }

        BigDecimal marginAmount = switch (commissionType) {
            case RATE -> sellingPrice
                    .multiply(commissionValue)
                    .divide(PERCENT_DENOMINATOR, MONEY_SCALE, RoundingMode.HALF_UP);
            case FIXED_AMOUNT -> commissionValue.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        };
        BigDecimal unitSettlementAmount = sellingPrice.subtract(marginAmount);
        if (unitSettlementAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "개당 정산금액은 0 이상이어야 합니다.");
        }

        return new SettlementAmounts(marginAmount, unitSettlementAmount);
    }

    private int toPageIndex(int page) {
        return Math.max(page - FIRST_PAGE, FIRST_PAGE_INDEX);
    }

    private void validatePageSize(int size) {
        if (size < MIN_PAGE_SIZE) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "페이지 크기는 1 이상이어야 합니다.");
        }
    }

    private void validateDuplicateProductIds(CreateArtistShipmentRequest request) {
        List<Long> productIds = request.getItems().stream()
                .map(CreateArtistShipmentRequest.Item::getProductId)
                .toList();
        if (new HashSet<>(productIds).size() != productIds.size()) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "중복된 상품 ID가 포함되어 있습니다.");
        }
    }

    private String normalizeSearchKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }
        return keyword.trim().toLowerCase();
    }

    private record SettlementAmounts(
            BigDecimal marginAmount,
            BigDecimal unitSettlementAmount
    ) {
    }
}
