package app.dearobjet.backend.domain.contract;

import app.dearobjet.backend.domain.artist.entity.Artist;
import app.dearobjet.backend.domain.contract.dto.projection.ContractInventoryProductRow;
import app.dearobjet.backend.domain.contract.dto.projection.ContractInventoryRow;
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
        Artist rejectedArtist = createArtist(
                createUser("managed-rejected@test.com", "거절 작가", Role.ARTIST, "01091000005", null),
                "거절 작가",
                Specialty.INTERIOR_DECOR
        );

        ShopArtistContract pendingContract = createContract(
                pendingArtist,
                shop,
                ContractStatus.PENDING,
                null,
                null
        );
        ShopArtistContract approvedContract = createContract(
                approvedArtist,
                shop,
                ContractStatus.APPROVED,
                CONTRACT_START_DATE,
                CONTRACT_END_DATE
        );
        ShopArtistContract endedContract = createContract(
                endedArtist,
                shop,
                ContractStatus.ENDED,
                CONTRACT_START_DATE.minusYears(1),
                CONTRACT_END_DATE.minusYears(1)
        );
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
        assertThat(notOwned).isEmpty();
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
        ShopArtistContract contract = ShopArtistContract.builder()
                .artist(artist)
                .shop(shop)
                .contractStatus(contractStatus)
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

    private app.dearobjet.backend.domain.contract.dto.projection.ManagedArtistContractRow findManagedArtistRow(
            List<app.dearobjet.backend.domain.contract.dto.projection.ManagedArtistContractRow> rows,
            String artistName
    ) {
        return rows.stream()
                .filter(row -> artistName.equals(row.getArtistName()))
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
