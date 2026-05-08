package app.dearobjet.backend.domain.contract;

import app.dearobjet.backend.domain.artist.entity.Artist;
import app.dearobjet.backend.domain.contract.dto.projection.ArtistAccountSearchRow;
import app.dearobjet.backend.domain.contract.dto.projection.ArtistSuggestionRow;
import app.dearobjet.backend.domain.contract.dto.projection.ArtistShipmentProductRow;
import app.dearobjet.backend.domain.contract.dto.projection.ArtistShipmentShopRow;
import app.dearobjet.backend.domain.contract.dto.projection.ContractInventoryProductRow;
import app.dearobjet.backend.domain.contract.dto.projection.ContractInventoryRow;
import app.dearobjet.backend.domain.contract.dto.projection.ManagedShopContractRow;
import app.dearobjet.backend.domain.contract.entity.ContractProduct;
import app.dearobjet.backend.domain.contract.entity.ContractProductStockMovement;
import app.dearobjet.backend.domain.contract.entity.ShopArtistContract;
import app.dearobjet.backend.domain.contract.enums.CommissionType;
import app.dearobjet.backend.domain.contract.enums.ContractRequestType;
import app.dearobjet.backend.domain.contract.enums.ContractProductListingStatus;
import app.dearobjet.backend.domain.contract.enums.ContractProductStockMovementType;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import app.dearobjet.backend.domain.contract.repository.ContractProductRepository;
import app.dearobjet.backend.domain.contract.repository.ContractProductStockMovementRepository;
import app.dearobjet.backend.domain.shop.entity.Product;
import app.dearobjet.backend.domain.shop.entity.Shop;
import app.dearobjet.backend.domain.shop.enums.ProductStatus;
import app.dearobjet.backend.domain.user.entity.BusinessProfile;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.enums.BusinessCategory;
import app.dearobjet.backend.domain.user.enums.BusinessType;
import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.enums.Specialty;
import app.dearobjet.backend.domain.user.enums.UserStatus;
import app.dearobjet.backend.domain.user.repository.ArtistRepository;
import app.dearobjet.backend.domain.user.repository.BusinessProfileRepository;
import app.dearobjet.backend.domain.user.repository.ShopRepository;
import app.dearobjet.backend.domain.user.repository.UserRepository;
import app.dearobjet.backend.global.common.config.JpaConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
class ContractProductRepositoryTest {

    private static final BusinessType DEFAULT_BUSINESS_TYPE = BusinessType.WHOLESALE_RETAIL;
    private static final BusinessCategory DEFAULT_BUSINESS_CATEGORY = BusinessCategory.CRAFT_RETAIL;
    private static final long ACTOR_USER_ID = 101L;
    private static final int CUP_INBOUND_QUANTITY = 10;
    private static final int CUP_SALE_QUANTITY = 2;
    private static final int PLATE_INBOUND_QUANTITY = 5;
    private static final int PLATE_SALE_QUANTITY = 1;
    private static final BigDecimal CUP_PRICE = new BigDecimal("10000.00");
    private static final BigDecimal CUP_SELLING_PRICE = new BigDecimal("12000.00");
    private static final BigDecimal PLATE_PRICE = new BigDecimal("8000.00");
    private static final BigDecimal PLATE_SELLING_PRICE = new BigDecimal("9000.00");
    private static final BigDecimal DEFAULT_COMMISSION_VALUE = new BigDecimal("20.0000");
    private static final BigDecimal CUP_MARGIN_AMOUNT = new BigDecimal("2400.00");
    private static final BigDecimal CUP_UNIT_SETTLEMENT_AMOUNT = new BigDecimal("9600.00");
    private static final LocalDate CONTRACT_START_DATE = LocalDate.of(2026, 5, 1);
    private static final LocalDate CONTRACT_END_DATE = LocalDate.of(2026, 12, 31);
    private static final LocalDate QUERY_TODAY = LocalDate.of(2026, 5, 8);
    private static final LocalDateTime CUP_INBOUND_AT = LocalDateTime.of(2026, 4, 28, 10, 0);
    private static final LocalDateTime CUP_SALE_AT = LocalDateTime.of(2026, 4, 28, 12, 0);
    private static final LocalDateTime PLATE_INBOUND_AT = LocalDateTime.of(2026, 4, 27, 14, 30);
    private static final LocalDateTime PLATE_SALE_AT = LocalDateTime.of(2026, 4, 27, 16, 0);

    @Autowired
    private ContractProductRepository contractProductRepository;

    @Autowired
    private ContractProductStockMovementRepository contractProductStockMovementRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private BusinessProfileRepository businessProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("재고관리 목록은 승인 계약 작가를 품목 유무와 관계없이 반환한다")
    void givenApprovedContracts_whenQueryByShop_thenReturnContractedArtists() {
        InventoryFixture fixture = createInventoryFixture();

        List<ContractInventoryRow> rows = contractRepository.findInventoryRowsByShopId(
                fixture.shop().getShopId(),
                ContractStatus.APPROVED,
                ContractProductListingStatus.ACTIVE
        );

        assertThat(rows).hasSize(2);
        ContractInventoryRow artistRow = findArtistRow(rows, "Postman 도자기 작가");
        ContractInventoryRow noProductArtistRow = findArtistRow(rows, "Postman 문구 작가");

        assertThat(artistRow.getContractId()).isEqualTo(fixture.artistAContract().getShopArtistContractsId());
        assertThat(artistRow.getArtistImageUrl()).isEqualTo("https://image.test/artist-a.png");
        assertThat(artistRow.getSpecialty()).isEqualTo(Specialty.CERAMIC);
        assertThat(artistRow.getRecentStockedAt()).isEqualTo(CUP_INBOUND_AT);
        assertThat(artistRow.getInboundConfirmed()).isFalse();
        assertThat(noProductArtistRow.getContractId()).isEqualTo(fixture.artistBContract().getShopArtistContractsId());
        assertThat(noProductArtistRow.getSpecialty()).isEqualTo(Specialty.STATIONERY_PAPER);
        assertThat(noProductArtistRow.getRecentStockedAt()).isNull();
    }

    @Test
    @DisplayName("작가별 재고 상세는 총 입고 수량과 정산 필드를 반환한다")
    void givenContractProducts_whenQueryDetailRows_thenReturnInventoryProductRows() {
        InventoryFixture fixture = createInventoryFixture();

        List<ContractInventoryProductRow> rows = contractProductRepository.findInventoryProductRowsByContractId(
                fixture.artistAContract().getShopArtistContractsId(),
                fixture.shop().getShopId(),
                ContractStatus.APPROVED,
                ContractProductListingStatus.ACTIVE,
                ContractProductStockMovementType.INBOUND
        );

        assertThat(rows).hasSize(2);
        ContractInventoryProductRow cupRow = findProductRow(rows, "Postman Seed 도자기 컵");
        ContractInventoryProductRow plateRow = findProductRow(rows, "Postman Seed 미니 접시");

        assertThat(cupRow.getTotalQuantity()).isEqualTo((long) CUP_INBOUND_QUANTITY);
        assertThat(cupRow.getProductImageUrl()).isEqualTo("https://image.test/products/cup.png");
        assertThat(cupRow.getSellingPrice()).isEqualByComparingTo(CUP_SELLING_PRICE);
        assertThat(cupRow.getStockQuantity()).isEqualTo(CUP_INBOUND_QUANTITY - CUP_SALE_QUANTITY);
        assertThat(cupRow.getCommissionType()).isEqualTo(CommissionType.RATE);
        assertThat(cupRow.getCommissionValue()).isEqualByComparingTo(DEFAULT_COMMISSION_VALUE);
        assertThat(cupRow.getMarginAmount()).isEqualByComparingTo(CUP_MARGIN_AMOUNT);
        assertThat(cupRow.getUnitSettlementAmount()).isEqualByComparingTo(CUP_UNIT_SETTLEMENT_AMOUNT);
        assertThat(cupRow.getArtistName()).isEqualTo("Postman 도자기 작가");
        assertThat(cupRow.getRecentStockedAt()).isEqualTo(CUP_INBOUND_AT);

        assertThat(plateRow.getTotalQuantity()).isEqualTo((long) PLATE_INBOUND_QUANTITY);
        assertThat(plateRow.getStockQuantity()).isEqualTo(PLATE_INBOUND_QUANTITY - PLATE_SALE_QUANTITY);
        assertThat(plateRow.getRecentStockedAt()).isEqualTo(PLATE_INBOUND_AT);
    }

    @Test
    @DisplayName("작가 출고리스트는 활성 계약상품의 총 출고수량과 판매가를 반환한다")
    void givenArtistShipmentProducts_whenQueryByContract_thenReturnShipmentProductRows() {
        Artist artist = createArtist(
                createUser("shipment-products-artist@test.com", "출고상품 작가", Role.ARTIST, "01099000001", null),
                "출고상품 작가",
                Specialty.CERAMIC
        );
        Shop shop = createShop(
                createUser("shipment-products-shop@test.com", "출고상품 소품샵", Role.SHOP, "01099000002", null),
                "출고상품 소품샵",
                Specialty.HANDMADE_CRAFT
        );
        ShopArtistContract contract = createContract(artist, shop);
        Product shippedProduct = createProduct(
                artist,
                "출고된 세라믹 컵",
                CUP_PRICE,
                "https://image.test/products/shipped-cup.png"
        );
        Product pendingProduct = createProduct(
                artist,
                "출고 대기 접시",
                PLATE_PRICE,
                "https://image.test/products/pending-plate.png"
        );
        Product endedProduct = createProduct(
                artist,
                "종료된 상품",
                PLATE_PRICE,
                "https://image.test/products/ended.png"
        );
        ContractProduct shippedInventory = createContractProduct(
                contract,
                shippedProduct,
                CUP_SELLING_PRICE,
                CUP_MARGIN_AMOUNT,
                CUP_UNIT_SETTLEMENT_AMOUNT
        );
        ContractProduct pendingInventory = createContractProduct(
                contract,
                pendingProduct,
                PLATE_SELLING_PRICE,
                new BigDecimal("1800.00"),
                new BigDecimal("7200.00")
        );
        ContractProduct endedInventory = ContractProduct.builder()
                .shopArtistContract(contract)
                .product(endedProduct)
                .listingStatus(ContractProductListingStatus.ENDED)
                .sellingPrice(PLATE_SELLING_PRICE)
                .build();
        ContractProductStockMovement firstInbound =
                shippedInventory.applyInbound(CUP_INBOUND_QUANTITY, CUP_INBOUND_AT, ACTOR_USER_ID, "1차 출고");
        ContractProductStockMovement secondInbound =
                shippedInventory.applyInbound(PLATE_INBOUND_QUANTITY, PLATE_INBOUND_AT, ACTOR_USER_ID, "2차 출고");

        contractProductRepository.save(shippedInventory);
        contractProductRepository.save(pendingInventory);
        contractProductRepository.save(endedInventory);
        saveMovement(firstInbound);
        saveMovement(secondInbound);
        flushAndClear();

        List<ArtistShipmentProductRow> rows = contractProductRepository.findShipmentProductRowsByContractId(
                contract.getShopArtistContractsId(),
                artist.getId(),
                ContractStatus.APPROVED,
                ContractProductListingStatus.ACTIVE,
                ContractProductStockMovementType.INBOUND
        );

        assertThat(rows).hasSize(2);
        ArtistShipmentProductRow shippedRow = findShipmentProductRow(rows, "출고된 세라믹 컵");
        ArtistShipmentProductRow pendingRow = findShipmentProductRow(rows, "출고 대기 접시");

        assertThat(shippedRow.getTotalShipmentQuantity()).isEqualTo(CUP_INBOUND_QUANTITY + PLATE_INBOUND_QUANTITY);
        assertThat(shippedRow.getProductImageUrl()).isEqualTo("https://image.test/products/shipped-cup.png");
        assertThat(shippedRow.getSellingPrice()).isEqualByComparingTo(CUP_SELLING_PRICE);
        assertThat(shippedRow.getCommissionType()).isEqualTo(CommissionType.RATE);
        assertThat(shippedRow.getCommissionValue()).isEqualByComparingTo(DEFAULT_COMMISSION_VALUE);
        assertThat(shippedRow.getUnitSettlementAmount()).isEqualByComparingTo(CUP_UNIT_SETTLEMENT_AMOUNT);
        assertThat(pendingRow.getTotalShipmentQuantity()).isZero();
        assertThat(pendingRow.getUnitSettlementAmount()).isEqualByComparingTo(new BigDecimal("7200.00"));
        assertThat(rows).extracting("productName").doesNotContain("종료된 상품");
    }

    @Test
    @DisplayName("출고리스트는 소품샵의 계약 이력을 계약별 품목 상세로 조회한다")
    void givenShopContractHistory_whenQueryShipmentProducts_thenReturnRowsByContract() {
        Artist artist = createArtist(
                createUser("shipment-history-artist@test.com", "출고이력 작가", Role.ARTIST, "01097300001", null),
                "출고이력 작가",
                Specialty.CERAMIC
        );
        Shop shop = createShop(
                createUser("shipment-history-shop@test.com", "출고이력 소품샵", Role.SHOP, "01097300002", null),
                "출고이력 소품샵",
                Specialty.HANDMADE_CRAFT
        );
        Shop otherShop = createShop(
                createUser("shipment-history-other-shop@test.com", "출고이력 다른샵", Role.SHOP, "01097300003", null),
                "출고이력 다른샵",
                Specialty.LIVING_GOODS
        );
        ShopArtistContract approvedContract = createContract(
                artist,
                shop,
                ContractStatus.APPROVED,
                ContractRequestType.NONE,
                CONTRACT_START_DATE,
                CONTRACT_END_DATE
        );
        ShopArtistContract endedContract = createContract(
                artist,
                shop,
                ContractStatus.ENDED,
                ContractRequestType.NONE,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 1)
        );
        ShopArtistContract terminatedContract = createContract(
                artist,
                shop,
                ContractStatus.TERMINATED,
                ContractRequestType.NONE,
                LocalDate.of(2026, 2, 2),
                LocalDate.of(2026, 3, 1)
        );
        createContract(artist, shop, ContractStatus.PENDING, ContractRequestType.NONE, null, null);
        createContract(artist, shop, ContractStatus.REJECTED, ContractRequestType.NONE, null, null);
        createContract(artist, otherShop, ContractStatus.APPROVED, ContractRequestType.NONE, null, null);

        Product approvedProduct = createProduct(
                artist,
                "현재 계약 컵",
                CUP_PRICE,
                "https://image.test/products/current-cup.png"
        );
        Product endedProduct = createProduct(
                artist,
                "만료 계약 접시",
                PLATE_PRICE,
                "https://image.test/products/ended-plate.png"
        );
        Product terminatedProduct = createProduct(
                artist,
                "해지 계약 화병",
                PLATE_PRICE,
                "https://image.test/products/terminated-vase.png"
        );
        ContractProduct approvedInventory = createContractProduct(
                approvedContract,
                approvedProduct,
                CUP_SELLING_PRICE,
                CUP_MARGIN_AMOUNT,
                CUP_UNIT_SETTLEMENT_AMOUNT
        );
        ContractProduct endedInventory = ContractProduct.builder()
                .shopArtistContract(endedContract)
                .product(endedProduct)
                .listingStatus(ContractProductListingStatus.ENDED)
                .sellingPrice(PLATE_SELLING_PRICE)
                .unitSettlementAmount(new BigDecimal("7200.00"))
                .build();
        ContractProduct terminatedInventory = createContractProduct(
                terminatedContract,
                terminatedProduct,
                PLATE_SELLING_PRICE,
                new BigDecimal("1800.00"),
                new BigDecimal("7200.00")
        );

        ContractProductStockMovement approvedInbound =
                approvedInventory.applyInbound(CUP_INBOUND_QUANTITY, CUP_INBOUND_AT, ACTOR_USER_ID, "현재 계약 입고");
        ContractProductStockMovement terminatedInbound =
                terminatedInventory.applyInbound(PLATE_INBOUND_QUANTITY, PLATE_INBOUND_AT, ACTOR_USER_ID, "해지 계약 입고");
        contractProductRepository.save(approvedInventory);
        contractProductRepository.save(endedInventory);
        contractProductRepository.save(terminatedInventory);
        saveMovement(approvedInbound);
        saveMovement(terminatedInbound);
        flushAndClear();

        List<ShopArtistContract> contracts = contractRepository.findShipmentProductContractsByArtistIdAndShopId(
                artist.getId(),
                shop.getShopId(),
                List.of(ContractStatus.APPROVED, ContractStatus.ENDED, ContractStatus.TERMINATED)
        );
        List<ArtistShipmentProductRow> rows = contractProductRepository.findShipmentProductRowsByContractIds(
                contracts.stream()
                        .map(ShopArtistContract::getShopArtistContractsId)
                        .toList(),
                artist.getId(),
                ContractProductStockMovementType.INBOUND
        );

        assertThat(contracts).extracting(ShopArtistContract::getContractStatus)
                .containsExactly(ContractStatus.TERMINATED, ContractStatus.ENDED, ContractStatus.APPROVED);
        assertThat(rows).extracting("productName")
                .containsExactly("해지 계약 화병", "만료 계약 접시", "현재 계약 컵");
        assertThat(findShipmentProductRow(rows, "현재 계약 컵").getContractId())
                .isEqualTo(approvedContract.getShopArtistContractsId());
        assertThat(findShipmentProductRow(rows, "현재 계약 컵").getTotalShipmentQuantity())
                .isEqualTo(CUP_INBOUND_QUANTITY);
        assertThat(findShipmentProductRow(rows, "만료 계약 접시").getContractId())
                .isEqualTo(endedContract.getShopArtistContractsId());
        assertThat(findShipmentProductRow(rows, "만료 계약 접시").getTotalShipmentQuantity()).isZero();
        assertThat(findShipmentProductRow(rows, "해지 계약 화병").getContractId())
                .isEqualTo(terminatedContract.getShopArtistContractsId());
        assertThat(findShipmentProductRow(rows, "해지 계약 화병").getTotalShipmentQuantity())
                .isEqualTo(PLATE_INBOUND_QUANTITY);
    }

    @Test
    @DisplayName("출고관리 입점 매장 목록은 승인 계약 소품샵만 반환한다")
    void givenArtistContracts_whenQueryShipmentShops_thenReturnApprovedShopsOnly() {
        Artist artist = createArtist(
                createUser("shipment-artist@test.com", "출고관리 작가", Role.ARTIST, "01097000001", null),
                "출고관리 작가",
                Specialty.CERAMIC
        );
        Shop approvedShop = createShop(
                createUser("shipment-approved-shop@test.com", "승인 소품샵", Role.SHOP, "01097000002", null),
                "승인 소품샵",
                Specialty.HANDMADE_CRAFT
        );
        Shop pendingShop = createShop(
                createUser("shipment-pending-shop@test.com", "대기 소품샵", Role.SHOP, "01097000003", null),
                "대기 소품샵",
                Specialty.LIVING_GOODS
        );
        Shop endedShop = createShop(
                createUser("shipment-ended-shop@test.com", "만료 소품샵", Role.SHOP, "01097000004", null),
                "만료 소품샵",
                Specialty.STATIONERY_PAPER
        );
        ShopArtistContract approvedContract = createContract(
                artist,
                approvedShop,
                ContractStatus.APPROVED,
                ContractRequestType.NONE,
                CONTRACT_START_DATE,
                CONTRACT_END_DATE
        );
        createContract(artist, pendingShop, ContractStatus.PENDING, ContractRequestType.NONE, null, null);
        createContract(artist, endedShop, ContractStatus.ENDED, ContractRequestType.NONE, null, null);
        flushAndClear();

        List<ArtistShipmentShopRow> rows = contractRepository.findShipmentShopRowsByArtistId(
                artist.getId(),
                ContractStatus.APPROVED,
                ContractProductListingStatus.ACTIVE,
                QUERY_TODAY
        );

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getContractId()).isEqualTo(approvedContract.getShopArtistContractsId());
        assertThat(rows.get(0).getShopName()).isEqualTo("승인 소품샵");
        assertThat(rows.get(0).getSpecialty()).isEqualTo(Specialty.HANDMADE_CRAFT);
        assertThat(rows.get(0).getContractStartDate()).isEqualTo(CONTRACT_START_DATE);
        assertThat(rows.get(0).getContractEndDate()).isEqualTo(CONTRACT_END_DATE);
        assertThat(rows.get(0).getRecentStockedAt()).isNull();
    }

    @Test
    @DisplayName("출고관리 입점 매장 목록은 같은 소품샵을 중복 반환하지 않는다")
    void givenDuplicatedApprovedShopContracts_whenQueryShipmentShops_thenReturnLatestShopOnly() {
        Artist artist = createArtist(
                createUser("shipment-dedup-artist@test.com", "출고중복 작가", Role.ARTIST, "01097100001", null),
                "출고중복 작가",
                Specialty.CERAMIC
        );
        Shop shop = createShop(
                createUser("shipment-dedup-shop@test.com", "중복 소품샵", Role.SHOP, "01097100002", null),
                "중복 소품샵",
                Specialty.HANDMADE_CRAFT
        );
        ShopArtistContract oldContract = createContract(
                artist,
                shop,
                ContractStatus.APPROVED,
                ContractRequestType.NONE,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 30)
        );
        ShopArtistContract latestContract = createContract(
                artist,
                shop,
                ContractStatus.APPROVED,
                ContractRequestType.NONE,
                CONTRACT_START_DATE,
                CONTRACT_END_DATE
        );
        flushAndClear();

        List<ArtistShipmentShopRow> rows = contractRepository.findShipmentShopRowsByArtistId(
                artist.getId(),
                ContractStatus.APPROVED,
                ContractProductListingStatus.ACTIVE,
                QUERY_TODAY
        );

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getShopId()).isEqualTo(shop.getShopId());
        assertThat(rows.get(0).getContractId()).isEqualTo(latestContract.getShopArtistContractsId());
        assertThat(rows.get(0).getContractId()).isNotEqualTo(oldContract.getShopArtistContractsId());
    }

    @Test
    @DisplayName("작가 출고리스트 조회용 계약은 소품샵 기준 최신 승인 계약을 반환한다")
    void givenApprovedShopContract_whenFindByArtistAndShop_thenReturnLatestContract() {
        Artist artist = createArtist(
                createUser("shipment-shop-contract-artist@test.com", "소품샵계약 작가", Role.ARTIST, "01097200001", null),
                "소품샵계약 작가",
                Specialty.CERAMIC
        );
        Shop shop = createShop(
                createUser("shipment-shop-contract-shop@test.com", "소품샵계약 매장", Role.SHOP, "01097200002", null),
                "소품샵계약 매장",
                Specialty.HANDMADE_CRAFT
        );
        ShopArtistContract oldContract = createContract(artist, shop);
        ShopArtistContract latestContract = createContract(artist, shop);
        createContract(artist, shop, ContractStatus.PENDING, ContractRequestType.NONE, null, null);
        flushAndClear();

        List<ShopArtistContract> contracts = contractRepository.findApprovedContractsByArtistIdAndShopId(
                artist.getId(),
                shop.getShopId(),
                ContractStatus.APPROVED
        );

        assertThat(contracts).extracting(ShopArtistContract::getShopArtistContractsId)
                .containsExactly(
                        latestContract.getShopArtistContractsId(),
                        oldContract.getShopArtistContractsId()
                );
    }

    @Test
    @DisplayName("출고관리 입점 매장 목록은 최근 입고일 기준으로 정렬한다")
    void givenShipmentShopsWithInbound_whenQueryShipmentShops_thenSortByRecentInbound() {
        Artist artist = createArtist(
                createUser("shipment-sort-artist@test.com", "출고정렬 작가", Role.ARTIST, "01098000001", null),
                "출고정렬 작가",
                Specialty.CERAMIC
        );
        Shop recentShop = createShop(
                createUser("shipment-sort-recent@test.com", "최근 입고 소품샵", Role.SHOP, "01098000002", null),
                "최근 입고 소품샵",
                Specialty.CERAMIC
        );
        Shop oldShop = createShop(
                createUser("shipment-sort-old@test.com", "이전 입고 소품샵", Role.SHOP, "01098000003", null),
                "이전 입고 소품샵",
                Specialty.HANDMADE_CRAFT
        );
        Shop holdShop = createShop(
                createUser("shipment-sort-hold@test.com", "보류 소품샵", Role.SHOP, "01098000004", null),
                "보류 소품샵",
                Specialty.LIVING_GOODS
        );
        ShopArtistContract recentContract = createContract(artist, recentShop);
        ShopArtistContract oldContract = createContract(artist, oldShop);
        ShopArtistContract holdContract = createContract(artist, holdShop);
        Product recentProduct = createProduct(
                artist,
                "최근 입고 상품",
                CUP_PRICE,
                "https://image.test/products/recent.png"
        );
        Product oldProduct = createProduct(
                artist,
                "이전 입고 상품",
                PLATE_PRICE,
                "https://image.test/products/old.png"
        );
        ContractProduct recentInventory = createContractProduct(
                recentContract,
                recentProduct,
                CUP_SELLING_PRICE,
                CUP_MARGIN_AMOUNT,
                CUP_UNIT_SETTLEMENT_AMOUNT
        );
        ContractProduct oldInventory = createContractProduct(
                oldContract,
                oldProduct,
                PLATE_SELLING_PRICE,
                new BigDecimal("1800.00"),
                new BigDecimal("7200.00")
        );
        ContractProductStockMovement recentInbound =
                recentInventory.applyInbound(CUP_INBOUND_QUANTITY, CUP_INBOUND_AT, ACTOR_USER_ID, "최근 입고");
        ContractProductStockMovement oldInbound =
                oldInventory.applyInbound(PLATE_INBOUND_QUANTITY, PLATE_INBOUND_AT, ACTOR_USER_ID, "이전 입고");
        contractProductRepository.save(recentInventory);
        contractProductRepository.save(oldInventory);
        saveMovement(recentInbound);
        saveMovement(oldInbound);
        flushAndClear();

        List<ArtistShipmentShopRow> rows = contractRepository.findShipmentShopRowsByArtistId(
                artist.getId(),
                ContractStatus.APPROVED,
                ContractProductListingStatus.ACTIVE,
                QUERY_TODAY
        );

        assertThat(rows).extracting("contractId")
                .containsExactly(
                        recentContract.getShopArtistContractsId(),
                        oldContract.getShopArtistContractsId(),
                        holdContract.getShopArtistContractsId()
                );
        assertThat(rows.get(0).getRecentStockedAt()).isEqualTo(CUP_INBOUND_AT);
        assertThat(rows.get(1).getRecentStockedAt()).isEqualTo(PLATE_INBOUND_AT);
        assertThat(rows.get(2).getRecentStockedAt()).isNull();
    }

    @Test
    @DisplayName("입점관리 작가 목록은 대기, 승인, 만료 계약만 반환한다")
    void givenManagedContracts_whenQueryByShop_thenReturnPendingApprovedEndedOnly() {
        User shopUser = createUser(
                "managed-shop@test.com",
                "입점관리 소품샵",
                Role.SHOP,
                "01091000001",
                "https://image.test/managed-shop.png"
        );
        Shop shop = createShop(shopUser, "입점관리 테스트 소품샵", Specialty.LIVING_GOODS);
        Artist pendingArtist = createArtist(
                createUser("managed-pending@test.com", "대기 작가", Role.ARTIST, "01091000002", null),
                "대기 작가",
                Specialty.CERAMIC
        );
        Artist approvedArtist = createArtist(
                createUser("managed-approved@test.com", "계약 작가", Role.ARTIST, "01091000003", null),
                "계약 작가",
                Specialty.HANDMADE_CRAFT
        );
        Artist endedArtist = createArtist(
                createUser("managed-ended@test.com", "만료 작가", Role.ARTIST, "01091000004", null),
                "만료 작가",
                Specialty.FABRIC_TEXTILE
        );
        Artist terminatedArtist = createArtist(
                createUser("managed-terminated@test.com", "해지 작가", Role.ARTIST, "01091000005", null),
                "해지 작가",
                Specialty.INTERIOR_DECOR
        );
        Artist rejectedArtist = createArtist(
                createUser("managed-rejected@test.com", "거절 작가", Role.ARTIST, "01091000006", null),
                "거절 작가",
                Specialty.INTERIOR_DECOR
        );

        ShopArtistContract pendingContract = createContract(
                pendingArtist,
                shop,
                ContractStatus.PENDING,
                ContractRequestType.EXTENSION,
                null,
                null
        );
        ShopArtistContract approvedContract = createContract(
                approvedArtist,
                shop,
                ContractStatus.APPROVED,
                ContractRequestType.NONE,
                CONTRACT_START_DATE,
                CONTRACT_END_DATE
        );
        ShopArtistContract endedContract = createContract(
                endedArtist,
                shop,
                ContractStatus.ENDED,
                ContractRequestType.NONE,
                CONTRACT_START_DATE.minusYears(1),
                CONTRACT_END_DATE.minusYears(1)
        );
        createContract(terminatedArtist, shop, ContractStatus.TERMINATED, ContractRequestType.NONE, null, null);
        createContract(rejectedArtist, shop, ContractStatus.REJECTED, null, null);
        flushAndClear();

        var rows = contractRepository.findManagedArtistRowsByShopId(
                shop.getShopId(),
                List.of(ContractStatus.PENDING, ContractStatus.APPROVED, ContractStatus.ENDED),
                ContractStatus.PENDING,
                ContractStatus.APPROVED,
                ContractStatus.ENDED
        );

        assertThat(rows).hasSize(3);
        assertThat(rows).extracting("contractId")
                .containsExactly(
                        pendingContract.getShopArtistContractsId(),
                        approvedContract.getShopArtistContractsId(),
                        endedContract.getShopArtistContractsId()
                );
        assertThat(findManagedArtistRow(rows, "계약 작가").getContractStartDate()).isEqualTo(CONTRACT_START_DATE);
        assertThat(findManagedArtistRow(rows, "계약 작가").getContractEndDate()).isEqualTo(CONTRACT_END_DATE);
        assertThat(findManagedArtistRow(rows, "계약 작가").getContractStatus()).isEqualTo(ContractStatus.APPROVED);
        assertThat(findManagedArtistRow(rows, "대기 작가").getContractRequestType())
                .isEqualTo(ContractRequestType.EXTENSION);
    }

    @Test
    @DisplayName("입점관리 계약서 상세는 해당 소품샵 계약만 조회한다")
    void givenManagedContract_whenQueryDetailByShop_thenReturnOwnedContract() {
        User shopUser = createUser(
                "managed-detail-shop@test.com",
                "상세 소품샵",
                Role.SHOP,
                "01092000001",
                "https://image.test/detail-shop.png"
        );
        Shop shop = createShop(shopUser, "상세 테스트 소품샵", Specialty.LIVING_GOODS);
        Artist artist = createArtist(
                createUser("managed-detail-artist@test.com", "상세 작가", Role.ARTIST, "01092000002", null),
                "상세 작가",
                Specialty.CERAMIC
        );
        ShopArtistContract contract = createContract(
                artist,
                shop,
                ContractStatus.APPROVED,
                ContractRequestType.RELEASE,
                CONTRACT_START_DATE,
                CONTRACT_END_DATE
        );
        flushAndClear();

        var found = contractRepository.findManagedArtistContractByIdAndShopId(
                contract.getShopArtistContractsId(),
                shop.getShopId(),
                List.of(ContractStatus.PENDING, ContractStatus.APPROVED, ContractStatus.ENDED)
        );
        var notOwned = contractRepository.findManagedArtistContractByIdAndShopId(
                contract.getShopArtistContractsId(),
                shop.getShopId() + 1,
                List.of(ContractStatus.PENDING, ContractStatus.APPROVED, ContractStatus.ENDED)
        );

        assertThat(found).isPresent();
        assertThat(found.get().getArtist().getBusinessProfile().getBusinessName()).isEqualTo("상세 작가");
        assertThat(found.get().getShop().getBusinessName()).isEqualTo("상세 테스트 소품샵");
        assertThat(found.get().getContractStartDate()).isEqualTo(CONTRACT_START_DATE);
        assertThat(found.get().getContractEndDate()).isEqualTo(CONTRACT_END_DATE);
        assertThat(found.get().getContractRequestType()).isEqualTo(ContractRequestType.RELEASE);
        assertThat(notOwned).isEmpty();
    }

    @Test
    @DisplayName("입점 작가 제안 목록은 현재 진행중 계약이 없는 활성 작가만 반환한다")
    void givenArtistsAndContracts_whenQuerySuggestions_thenExcludeCurrentShopActiveContractsOnly() {
        User shopUser = createUser(
                "suggestion-shop@test.com",
                "추천 소품샵",
                Role.SHOP,
                "01093000001",
                null
        );
        User otherShopUser = createUser(
                "suggestion-other-shop@test.com",
                "다른 소품샵",
                Role.SHOP,
                "01093000002",
                null
        );
        Shop shop = createShop(shopUser, "추천 테스트 소품샵", Specialty.LIVING_GOODS);
        Shop otherShop = createShop(otherShopUser, "다른 테스트 소품샵", Specialty.LIVING_GOODS);

        Artist eligibleArtist = createArtist(
                createUser("suggestion-eligible@test.com", "추천 가능 작가", Role.ARTIST, "01093000003", null),
                "추천 가능 작가",
                Specialty.CERAMIC
        );
        Artist rejectedArtist = createArtist(
                createUser("suggestion-rejected@test.com", "거절 이력 작가", Role.ARTIST, "01093000004", null),
                "거절 이력 작가",
                Specialty.HANDMADE_CRAFT
        );
        Artist terminatedArtist = createArtist(
                createUser("suggestion-terminated@test.com", "해지 이력 작가", Role.ARTIST, "01093000005", null),
                "해지 이력 작가",
                Specialty.FABRIC_TEXTILE
        );
        Artist otherShopContractArtist = createArtist(
                createUser("suggestion-other-contract@test.com", "다른샵 계약 작가", Role.ARTIST, "01093000006", null),
                "다른샵 계약 작가",
                Specialty.INTERIOR_DECOR
        );
        Artist pendingArtist = createArtist(
                createUser("suggestion-pending@test.com", "대기 제외 작가", Role.ARTIST, "01093000007", null),
                "대기 제외 작가",
                Specialty.CERAMIC
        );
        Artist approvedArtist = createArtist(
                createUser("suggestion-approved@test.com", "계약 제외 작가", Role.ARTIST, "01093000008", null),
                "계약 제외 작가",
                Specialty.CERAMIC
        );
        Artist endedArtist = createArtist(
                createUser("suggestion-ended@test.com", "만료 가능 작가", Role.ARTIST, "01093000009", null),
                "만료 가능 작가",
                Specialty.CERAMIC
        );
        Artist expiredApprovedArtist = createArtist(
                createUser("suggestion-expired-approved@test.com", "종료일 경과 작가", Role.ARTIST, "01093000010", null),
                "종료일 경과 작가",
                Specialty.CERAMIC
        );

        createContract(rejectedArtist, shop, ContractStatus.REJECTED, ContractRequestType.NONE, null, null);
        createContract(terminatedArtist, shop, ContractStatus.TERMINATED, ContractRequestType.NONE, null, null);
        createContract(otherShopContractArtist, otherShop, ContractStatus.APPROVED, ContractRequestType.NONE, null, null);
        createContract(pendingArtist, shop, ContractStatus.PENDING, ContractRequestType.EXTENSION, null, null);
        createContract(approvedArtist, shop, ContractStatus.APPROVED, ContractRequestType.NONE, null, null);
        createContract(endedArtist, shop, ContractStatus.ENDED, ContractRequestType.NONE, null, null);
        createContract(
                expiredApprovedArtist,
                shop,
                ContractStatus.APPROVED,
                ContractRequestType.NONE,
                QUERY_TODAY.minusMonths(2),
                QUERY_TODAY.minusDays(1)
        );
        flushAndClear();

        var rows = contractRepository.findArtistSuggestionRows(
                shop.getShopId(),
                Role.ARTIST,
                UserStatus.ACTIVE,
                ContractStatus.PENDING,
                ContractStatus.APPROVED,
                QUERY_TODAY
        );

        assertThat(rows).extracting("artistName")
                .contains(
                        "추천 가능 작가",
                        "거절 이력 작가",
                        "해지 이력 작가",
                        "다른샵 계약 작가",
                        "만료 가능 작가",
                        "종료일 경과 작가"
                )
                .doesNotContain(
                        "대기 제외 작가",
                        "계약 제외 작가"
                );
        assertThat(findSuggestionRow(rows, "추천 가능 작가").getArtistId())
                .isEqualTo(eligibleArtist.getId());
        assertThat(findSuggestionRow(rows, "추천 가능 작가").getUserId())
                .isEqualTo(eligibleArtist.getUser().getId());
    }

    @Test
    @DisplayName("계약서 발송용 작가 계정 검색은 상호명, 사용자 이름, 이메일로 검색한다")
    void givenKeyword_whenSearchArtistAccounts_thenReturnMatchedActiveArtists() {
        User shopUser = createUser(
                "search-shop@test.com",
                "검색 소품샵",
                Role.SHOP,
                "01094000001",
                null
        );
        Shop shop = createShop(shopUser, "검색 테스트 소품샵", Specialty.LIVING_GOODS);
        Artist businessNameMatchedArtist = createArtist(
                createUser("search-business@test.com", "비즈니스 매칭 사용자", Role.ARTIST, "01094000002", null),
                "Postman 세라믹 스튜디오",
                Specialty.CERAMIC
        );
        Artist userNameMatchedArtist = createArtist(
                createUser("search-name@test.com", "Postman 이름 작가", Role.ARTIST, "01094000003", null),
                "이름 검색 스튜디오",
                Specialty.HANDMADE_CRAFT
        );
        Artist emailMatchedArtist = createArtist(
                createUser("postman-email@test.com", "이메일 검색 작가", Role.ARTIST, "01094000004", null),
                "이메일 검색 스튜디오",
                Specialty.FABRIC_TEXTILE
        );
        createArtist(
                createUser("search-customer@test.com", "Postman 일반 회원", Role.CUSTOMER, "01094000005", null),
                "일반 회원 스튜디오",
                Specialty.INTERIOR_DECOR
        );
        User inactiveUser = createUser(
                "search-inactive@test.com",
                "Postman 비활성 작가",
                Role.ARTIST,
                "01094000006",
                null
        );
        inactiveUser.deactivate();
        createArtist(inactiveUser, "비활성 스튜디오", Specialty.CERAMIC);
        flushAndClear();

        var rows = contractRepository.searchArtistAccountRows(
                shop.getShopId(),
                Role.ARTIST,
                UserStatus.ACTIVE,
                ContractStatus.PENDING,
                ContractStatus.APPROVED,
                QUERY_TODAY,
                "%postman%",
                PageRequest.of(0, 10)
        );

        assertThat(rows).extracting("artistName")
                .contains(
                        "Postman 세라믹 스튜디오",
                        "이름 검색 스튜디오",
                        "이메일 검색 스튜디오"
                )
                .doesNotContain(
                        "일반 회원 스튜디오",
                        "비활성 스튜디오"
                );
        assertThat(findAccountSearchRow(rows, "Postman 세라믹 스튜디오").getArtistId())
                .isEqualTo(businessNameMatchedArtist.getId());
        assertThat(findAccountSearchRow(rows, "이름 검색 스튜디오").getUserId())
                .isEqualTo(userNameMatchedArtist.getUser().getId());
        assertThat(findAccountSearchRow(rows, "이름 검색 스튜디오").getName())
                .isEqualTo("Postman 이름 작가");
        assertThat(findAccountSearchRow(rows, "이메일 검색 스튜디오").getEmail())
                .isEqualTo(emailMatchedArtist.getUser().getEmail());
    }

    @Test
    @DisplayName("계약서 발송용 작가 계정 검색은 현재 소품샵의 진행중 계약 작가를 제외한다")
    void givenCurrentShopContracts_whenSearchArtistAccounts_thenExcludePendingApprovedEndedOnly() {
        User shopUser = createUser(
                "search-contract-shop@test.com",
                "계약 검색 소품샵",
                Role.SHOP,
                "01095000001",
                null
        );
        User otherShopUser = createUser(
                "search-contract-other-shop@test.com",
                "계약 검색 다른샵",
                Role.SHOP,
                "01095000002",
                null
        );
        Shop shop = createShop(shopUser, "계약 검색 테스트 소품샵", Specialty.LIVING_GOODS);
        Shop otherShop = createShop(otherShopUser, "계약 검색 다른 소품샵", Specialty.LIVING_GOODS);

        Artist eligibleArtist = createArtist(
                createUser("account-eligible@test.com", "검색 가능 작가", Role.ARTIST, "01095000003", null),
                "계약 검색 가능 작가",
                Specialty.CERAMIC
        );
        Artist rejectedArtist = createArtist(
                createUser("account-rejected@test.com", "검색 거절 이력 작가", Role.ARTIST, "01095000004", null),
                "계약 검색 거절 이력 작가",
                Specialty.HANDMADE_CRAFT
        );
        Artist terminatedArtist = createArtist(
                createUser("account-terminated@test.com", "검색 해지 이력 작가", Role.ARTIST, "01095000005", null),
                "계약 검색 해지 이력 작가",
                Specialty.FABRIC_TEXTILE
        );
        Artist otherShopContractArtist = createArtist(
                createUser("account-other-shop@test.com", "검색 다른샵 계약 작가", Role.ARTIST, "01095000006", null),
                "계약 검색 다른샵 계약 작가",
                Specialty.INTERIOR_DECOR
        );
        Artist pendingArtist = createArtist(
                createUser("account-pending@test.com", "검색 대기 제외 작가", Role.ARTIST, "01095000007", null),
                "계약 검색 대기 제외 작가",
                Specialty.CERAMIC
        );
        Artist approvedArtist = createArtist(
                createUser("account-approved@test.com", "검색 승인 제외 작가", Role.ARTIST, "01095000008", null),
                "계약 검색 승인 제외 작가",
                Specialty.CERAMIC
        );
        Artist endedArtist = createArtist(
                createUser("account-ended@test.com", "검색 만료 가능 작가", Role.ARTIST, "01095000009", null),
                "계약 검색 만료 가능 작가",
                Specialty.CERAMIC
        );
        Artist expiredApprovedArtist = createArtist(
                createUser("account-expired-approved@test.com", "검색 종료일 경과 작가", Role.ARTIST, "01095000010", null),
                "계약 검색 종료일 경과 작가",
                Specialty.CERAMIC
        );

        createContract(rejectedArtist, shop, ContractStatus.REJECTED, ContractRequestType.NONE, null, null);
        createContract(terminatedArtist, shop, ContractStatus.TERMINATED, ContractRequestType.NONE, null, null);
        createContract(otherShopContractArtist, otherShop, ContractStatus.APPROVED, ContractRequestType.NONE, null, null);
        createContract(pendingArtist, shop, ContractStatus.PENDING, ContractRequestType.EXTENSION, null, null);
        createContract(approvedArtist, shop, ContractStatus.APPROVED, ContractRequestType.NONE, null, null);
        createContract(endedArtist, shop, ContractStatus.ENDED, ContractRequestType.NONE, null, null);
        createContract(
                expiredApprovedArtist,
                shop,
                ContractStatus.APPROVED,
                ContractRequestType.NONE,
                QUERY_TODAY.minusMonths(2),
                QUERY_TODAY.minusDays(1)
        );
        flushAndClear();

        var rows = contractRepository.searchArtistAccountRows(
                shop.getShopId(),
                Role.ARTIST,
                UserStatus.ACTIVE,
                ContractStatus.PENDING,
                ContractStatus.APPROVED,
                QUERY_TODAY,
                "%계약 검색%",
                PageRequest.of(0, 10)
        );

        assertThat(rows).extracting("artistName")
                .contains(
                        "계약 검색 가능 작가",
                        "계약 검색 거절 이력 작가",
                        "계약 검색 해지 이력 작가",
                        "계약 검색 다른샵 계약 작가",
                        "계약 검색 만료 가능 작가",
                        "계약 검색 종료일 경과 작가"
                )
                .doesNotContain(
                        "계약 검색 대기 제외 작가",
                        "계약 검색 승인 제외 작가"
                );
        assertThat(findAccountSearchRow(rows, "계약 검색 가능 작가").getArtistId())
                .isEqualTo(eligibleArtist.getId());
    }

    @Test
    @DisplayName("입점처 목록은 작가 소유 계약과 해지 후 14일 이내 계약만 반환한다")
    void givenArtistContracts_whenQueryManagedShops_thenReturnOwnedVisibleContractsOnly() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 7, 10, 0);
        User artistUser = createUser(
                "managed-shop-list-artist@test.com",
                "입점처 목록 작가",
                Role.ARTIST,
                "01096000001",
                null
        );
        User otherArtistUser = createUser(
                "managed-shop-list-other-artist@test.com",
                "다른 입점처 목록 작가",
                Role.ARTIST,
                "01096000002",
                null
        );
        Artist artist = createArtist(artistUser, "입점처 목록 작가", Specialty.CERAMIC);
        Artist otherArtist = createArtist(otherArtistUser, "다른 입점처 목록 작가", Specialty.HANDMADE_CRAFT);

        Shop pendingShop = createShop(
                createUser("managed-shop-list-pending@test.com", "대기 소품샵", Role.SHOP, "01096000003", null),
                "대기 소품샵",
                Specialty.LIVING_GOODS
        );
        Shop approvedShop = createShop(
                createUser("managed-shop-list-approved@test.com", "완료 소품샵", Role.SHOP, "01096000004", null),
                "완료 소품샵",
                Specialty.LIVING_GOODS
        );
        Shop endedShop = createShop(
                createUser("managed-shop-list-ended@test.com", "연장 소품샵", Role.SHOP, "01096000005", null),
                "연장 소품샵",
                Specialty.LIVING_GOODS
        );
        Shop terminatedRecentShop = createShop(
                createUser("managed-shop-list-terminated-recent@test.com", "최근 만료 소품샵", Role.SHOP, "01096000006", null),
                "최근 만료 소품샵",
                Specialty.LIVING_GOODS
        );
        Shop terminatedOldShop = createShop(
                createUser("managed-shop-list-terminated-old@test.com", "오래된 만료 소품샵", Role.SHOP, "01096000007", null),
                "오래된 만료 소품샵",
                Specialty.LIVING_GOODS
        );
        Shop rejectedShop = createShop(
                createUser("managed-shop-list-rejected@test.com", "거절 소품샵", Role.SHOP, "01096000008", null),
                "거절 소품샵",
                Specialty.LIVING_GOODS
        );
        Shop otherArtistShop = createShop(
                createUser("managed-shop-list-other-owner@test.com", "타 작가 소품샵", Role.SHOP, "01096000009", null),
                "타 작가 소품샵",
                Specialty.LIVING_GOODS
        );

        ShopArtistContract pendingContract = createContract(
                artist,
                pendingShop,
                ContractStatus.PENDING,
                ContractRequestType.EXTENSION,
                null,
                null
        );
        ShopArtistContract approvedContract = createContract(
                artist,
                approvedShop,
                ContractStatus.APPROVED,
                ContractRequestType.NONE,
                CONTRACT_START_DATE,
                CONTRACT_END_DATE
        );
        ShopArtistContract endedContract = createContract(
                artist,
                endedShop,
                ContractStatus.ENDED,
                ContractRequestType.NONE,
                CONTRACT_START_DATE.minusYears(1),
                CONTRACT_END_DATE.minusYears(1)
        );
        ShopArtistContract terminatedRecentContract = createContract(
                artist,
                terminatedRecentShop,
                ContractStatus.APPROVED,
                ContractRequestType.NONE,
                CONTRACT_START_DATE.minusYears(1),
                CONTRACT_END_DATE.minusYears(1)
        );
        terminatedRecentContract.terminate(now.minusDays(13));
        ShopArtistContract terminatedOldContract = createContract(
                artist,
                terminatedOldShop,
                ContractStatus.APPROVED,
                ContractRequestType.NONE,
                CONTRACT_START_DATE.minusYears(1),
                CONTRACT_END_DATE.minusYears(1)
        );
        terminatedOldContract.terminate(now.minusDays(15));
        createContract(artist, rejectedShop, ContractStatus.REJECTED, ContractRequestType.NONE, null, null);
        createContract(otherArtist, otherArtistShop, ContractStatus.APPROVED, ContractRequestType.NONE, null, null);
        flushAndClear();

        List<ManagedShopContractRow> rows = contractRepository.findManagedShopRowsByArtistId(
                artist.getId(),
                List.of(ContractStatus.PENDING, ContractStatus.APPROVED, ContractStatus.ENDED),
                ContractStatus.TERMINATED,
                now.minusDays(14),
                ContractStatus.PENDING,
                ContractStatus.APPROVED,
                ContractStatus.ENDED
        );

        assertThat(rows).extracting("contractId")
                .contains(
                        pendingContract.getShopArtistContractsId(),
                        approvedContract.getShopArtistContractsId(),
                        endedContract.getShopArtistContractsId(),
                        terminatedRecentContract.getShopArtistContractsId()
                )
                .doesNotContain(terminatedOldContract.getShopArtistContractsId());
        assertThat(rows).extracting("shopName")
                .contains("대기 소품샵", "완료 소품샵", "연장 소품샵", "최근 만료 소품샵")
                .doesNotContain("오래된 만료 소품샵", "거절 소품샵", "타 작가 소품샵");
        assertThat(findManagedShopRow(rows, "최근 만료 소품샵").getTerminatedAt())
                .isEqualTo(now.minusDays(13));
    }

    private InventoryFixture createInventoryFixture() {
        User shopUser = createUser(
                "postman-shop@test.com",
                "Postman 소품샵",
                Role.SHOP,
                "01090000001",
                "https://image.test/shop.png"
        );
        User artistAUser = createUser(
                "postman-artist-a@test.com",
                "Postman 작가 A",
                Role.ARTIST,
                "01090000002",
                "https://image.test/artist-a.png"
        );
        User artistBUser = createUser(
                "postman-artist-b@test.com",
                "Postman 작가 B",
                Role.ARTIST,
                "01090000003",
                "https://image.test/artist-b.png"
        );

        Shop shop = createShop(shopUser, "Postman 테스트 소품샵", Specialty.LIVING_GOODS);
        Artist artistA = createArtist(artistAUser, "Postman 도자기 작가", Specialty.CERAMIC);
        Artist artistB = createArtist(artistBUser, "Postman 문구 작가", Specialty.STATIONERY_PAPER);
        ShopArtistContract artistAContract = createContract(artistA, shop);
        ShopArtistContract artistBContract = createContract(artistB, shop);
        Product cup = createProduct(
                artistA,
                "Postman Seed 도자기 컵",
                CUP_PRICE,
                "https://image.test/products/cup.png"
        );
        Product plate = createProduct(
                artistA,
                "Postman Seed 미니 접시",
                PLATE_PRICE,
                "https://image.test/products/plate.png"
        );

        ContractProduct cupInventory = createContractProduct(
                artistAContract,
                cup,
                CUP_SELLING_PRICE,
                CUP_MARGIN_AMOUNT,
                CUP_UNIT_SETTLEMENT_AMOUNT
        );
        ContractProduct plateInventory = createContractProduct(
                artistAContract,
                plate,
                PLATE_SELLING_PRICE,
                new BigDecimal("1800.00"),
                new BigDecimal("7200.00")
        );

        ContractProductStockMovement cupInbound =
                cupInventory.applyInbound(CUP_INBOUND_QUANTITY, CUP_INBOUND_AT, ACTOR_USER_ID, "초기 입고");
        ContractProductStockMovement cupSale =
                cupInventory.applySaleDecrease(CUP_SALE_QUANTITY, CUP_SALE_AT, ACTOR_USER_ID, "판매 차감");
        ContractProductStockMovement plateInbound =
                plateInventory.applyInbound(PLATE_INBOUND_QUANTITY, PLATE_INBOUND_AT, ACTOR_USER_ID, "초기 입고");
        ContractProductStockMovement plateSale =
                plateInventory.applySaleDecrease(PLATE_SALE_QUANTITY, PLATE_SALE_AT, ACTOR_USER_ID, "판매 차감");

        contractProductRepository.save(cupInventory);
        contractProductRepository.save(plateInventory);
        saveMovement(cupInbound);
        saveMovement(cupSale);
        saveMovement(plateInbound);
        saveMovement(plateSale);
        flushAndClear();

        return new InventoryFixture(shop, artistAContract, artistBContract);
    }

    private User createUser(String email, String name, Role role, String phoneNumber, String profileUrl) {
        User user = User.builder()
                .email(email)
                .name(name)
                .phoneNumber(phoneNumber)
                .profileUrl(profileUrl)
                .role(role)
                .userStatus(UserStatus.ACTIVE)
                .build();
        return userRepository.save(user);
    }

    private Artist createArtist(User user, String businessName, Specialty specialty) {
        BusinessProfile businessProfile = createBusinessProfile(
                user,
                "ART-" + user.getId(),
                businessName,
                "서울시 성동구 테스트로",
                specialty
        );
        Artist artist = Artist.builder()
                .user(user)
                .businessProfile(businessProfile)
                .build();
        return artistRepository.save(artist);
    }

    private Shop createShop(User user, String shopName, Specialty specialty) {
        BusinessProfile businessProfile = createBusinessProfile(
                user,
                "SHOP-" + user.getId(),
                shopName,
                "서울시 마포구 테스트로",
                specialty
        );
        Shop shop = Shop.builder()
                .user(user)
                .businessProfile(businessProfile)
                .shopName(shopName)
                .shopDescription("API 테스트용 소품샵")
                .build();
        return shopRepository.save(shop);
    }

    private BusinessProfile createBusinessProfile(
            User user,
            String businessNumber,
            String businessName,
            String businessAddress,
            Specialty specialty
    ) {
        BusinessProfile businessProfile = BusinessProfile.builder()
                .user(user)
                .businessType(DEFAULT_BUSINESS_TYPE)
                .businessNumber(businessNumber)
                .businessName(businessName)
                .ownerName(user.getName())
                .businessAddress(businessAddress)
                .businessLicenseUrl("https://example.com/license/" + user.getId())
                .businessCategory(DEFAULT_BUSINESS_CATEGORY)
                .specialty(specialty)
                .reviewDataAgreement(Boolean.TRUE)
                .build();
        return businessProfileRepository.save(businessProfile);
    }

    private ShopArtistContract createContract(Artist artist, Shop shop) {
        return createContract(artist, shop, ContractStatus.APPROVED, null, null);
    }

    private ShopArtistContract createContract(
            Artist artist,
            Shop shop,
            ContractStatus contractStatus,
            LocalDate contractStartDate,
            LocalDate contractEndDate
    ) {
        return createContract(artist, shop, contractStatus, ContractRequestType.NONE, contractStartDate, contractEndDate);
    }

    private ShopArtistContract createContract(
            Artist artist,
            Shop shop,
            ContractStatus contractStatus,
            ContractRequestType contractRequestType,
            LocalDate contractStartDate,
            LocalDate contractEndDate
    ) {
        ShopArtistContract contract = ShopArtistContract.builder()
                .artist(artist)
                .shop(shop)
                .contractStatus(contractStatus)
                .contractRequestType(contractRequestType)
                .contractStartDate(contractStartDate)
                .contractEndDate(contractEndDate)
                .commissionType(CommissionType.RATE)
                .commissionValue(DEFAULT_COMMISSION_VALUE)
                .build();
        return contractRepository.save(contract);
    }

    private Product createProduct(Artist artist, String productName, BigDecimal price, String productUrl) {
        Product product = Product.builder()
                .artist(artist)
                .productName(productName)
                .price(price)
                .status(ProductStatus.ACTIVE)
                .productUrl(productUrl)
                .build();
        entityManager.persist(product);
        return product;
    }

    private ContractProduct createContractProduct(
            ShopArtistContract contract,
            Product product,
            BigDecimal sellingPrice,
            BigDecimal marginAmount,
            BigDecimal unitSettlementAmount
    ) {
        ContractProduct contractProduct = ContractProduct.builder()
                .shopArtistContract(contract)
                .product(product)
                .sellingPrice(sellingPrice)
                .build();
        contractProduct.updateInventoryManagement(marginAmount, unitSettlementAmount);
        return contractProduct;
    }

    private void saveMovement(ContractProductStockMovement movement) {
        contractProductStockMovementRepository.save(movement);
    }

    private ContractInventoryRow findArtistRow(List<ContractInventoryRow> rows, String artistName) {
        return rows.stream()
                .filter(row -> artistName.equals(row.getArtistName()))
                .findFirst()
                .orElseThrow();
    }

    private ContractInventoryProductRow findProductRow(
            List<ContractInventoryProductRow> rows,
            String productName
    ) {
        return rows.stream()
                .filter(row -> productName.equals(row.getProductName()))
                .findFirst()
                .orElseThrow();
    }

    private ArtistShipmentProductRow findShipmentProductRow(
            List<ArtistShipmentProductRow> rows,
            String productName
    ) {
        return rows.stream()
                .filter(row -> productName.equals(row.getProductName()))
                .findFirst()
                .orElseThrow();
    }

    private app.dearobjet.backend.domain.contract.dto.projection.ManagedArtistContractRow findManagedArtistRow(
            List<app.dearobjet.backend.domain.contract.dto.projection.ManagedArtistContractRow> rows,
            String artistName
    ) {
        return rows.stream()
                .filter(row -> artistName.equals(row.getArtistName()))
                .findFirst()
                .orElseThrow();
    }

    private ArtistSuggestionRow findSuggestionRow(List<ArtistSuggestionRow> rows, String artistName) {
        return rows.stream()
                .filter(row -> artistName.equals(row.getArtistName()))
                .findFirst()
                .orElseThrow();
    }

    private ArtistAccountSearchRow findAccountSearchRow(List<ArtistAccountSearchRow> rows, String artistName) {
        return rows.stream()
                .filter(row -> artistName.equals(row.getArtistName()))
                .findFirst()
                .orElseThrow();
    }

    private ManagedShopContractRow findManagedShopRow(List<ManagedShopContractRow> rows, String shopName) {
        return rows.stream()
                .filter(row -> shopName.equals(row.getShopName()))
                .findFirst()
                .orElseThrow();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private record InventoryFixture(
            Shop shop,
            ShopArtistContract artistAContract,
            ShopArtistContract artistBContract
    ) {
    }
}
