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
import app.dearobjet.backend.global.exception.InvalidInputException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
class ContractProductRepositoryTest {

    private static final BusinessType DEFAULT_BUSINESS_TYPE = BusinessType.WHOLESALE_RETAIL;
    private static final BusinessCategory DEFAULT_BUSINESS_CATEGORY = BusinessCategory.CRAFT_RETAIL;
    private static final Specialty DEFAULT_SPECIALTY = Specialty.HANDMADE_CRAFT;
    private static final int INITIAL_STOCK_QUANTITY = 5;
    private static final int INBOUND_QUANTITY = 7;
    private static final int SALE_QUANTITY = 3;
    private static final int RETURN_QUANTITY = 2;
    private static final int ADJUSTMENT_QUANTITY = 2;
    private static final int OVERSELL_EXTRA_QUANTITY = 1;
    private static final long ACTOR_USER_ID = 101L;
    private static final BigDecimal DEFAULT_PRODUCT_PRICE = new BigDecimal("10000.00");
    private static final BigDecimal DEFAULT_SELLING_PRICE = new BigDecimal("12000.00");
    private static final BigDecimal DEFAULT_COMMISSION_VALUE = new BigDecimal("20.0000");
    private static final BigDecimal UPDATED_MARGIN_AMOUNT = new BigDecimal("3000.00");
    private static final BigDecimal UPDATED_UNIT_SETTLEMENT_AMOUNT = new BigDecimal("9000.00");
    private static final LocalDateTime BASE_STOCKED_AT = LocalDateTime.of(2026, 4, 8, 10, 30);
    private static final LocalDateTime INBOUND_AT = LocalDateTime.of(2026, 4, 10, 9, 15);
    private static final LocalDateTime SALE_AT = LocalDateTime.of(2026, 4, 11, 14, 20);
    private static final LocalDateTime RETURN_AT = LocalDateTime.of(2026, 4, 12, 11, 0);
    private static final LocalDateTime ADJUSTMENT_AT = LocalDateTime.of(2026, 4, 13, 16, 5);

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
    @DisplayName("ENUM과 BigDecimal 필드를 저장하고 다시 조회할 수 있다")
    void givenInventoryEntities_whenPersist_thenSaveEnumAndBigDecimalFields() {
        User artistUser = createUser("artist1@example.com", "작가1", Role.ARTIST, "010-1111-1111", "https://image.test/artist1.png");
        User shopUser = createUser("shop1@example.com", "상점1", Role.SHOP, "010-9999-9999", null);
        Artist artist = createArtist(artistUser, "작가 상호");
        Shop shop = createShop(shopUser, "입점 상점");
        ShopArtistContract contract = createContract(artist, shop, ContractStatus.APPROVED);
        Product product = createProduct(artist, "도자기 컵");

        ContractProduct contractProduct = ContractProduct.builder()
                .shopArtistContract(contract)
                .product(product)
                .sellingPrice(DEFAULT_SELLING_PRICE)
                .build();
        contractProduct.updateInventoryManagement(UPDATED_MARGIN_AMOUNT, UPDATED_UNIT_SETTLEMENT_AMOUNT);
        ContractProductStockMovement inboundMovement = contractProduct.applyInbound(
                INBOUND_QUANTITY,
                BASE_STOCKED_AT,
                ACTOR_USER_ID,
                "초기 입고"
        );

        ContractProduct saved = contractProductRepository.save(contractProduct);
        contractProductStockMovementRepository.save(inboundMovement);
        flushAndClear();

        ContractProduct found = contractProductRepository.findById(saved.getContractProductsId()).orElseThrow();
        ContractProductStockMovement savedMovement = contractProductStockMovementRepository.findAll().get(0);

        assertThat(found.getProduct().getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(found.getShopArtistContract().getContractStatus()).isEqualTo(ContractStatus.APPROVED);
        assertThat(found.getShopArtistContract().getCommissionType()).isEqualTo(CommissionType.RATE);
        assertThat(found.getListingStatus()).isEqualTo(ContractProductListingStatus.ACTIVE);
        assertThat(found.getProduct().getPrice()).isEqualByComparingTo(DEFAULT_PRODUCT_PRICE);
        assertThat(found.getSellingPrice()).isEqualByComparingTo(DEFAULT_SELLING_PRICE);
        assertThat(found.getMarginAmount()).isEqualByComparingTo(UPDATED_MARGIN_AMOUNT);
        assertThat(found.getUnitSettlementAmount()).isEqualByComparingTo(UPDATED_UNIT_SETTLEMENT_AMOUNT);
        assertThat(found.getRecentStockedAt()).isEqualTo(BASE_STOCKED_AT);
        assertThat(found.getStockQuantity()).isEqualTo(INBOUND_QUANTITY);
        assertThat(found.getVersion()).isNotNull();
        assertThat(savedMovement.getMovementType()).isEqualTo(ContractProductStockMovementType.INBOUND);
        assertThat(savedMovement.getResultStockQuantity()).isEqualTo(INBOUND_QUANTITY);
    }

    @Test
    @DisplayName("같은 계약의 같은 품목은 하나의 현재 재고 행만 가질 수 있다")
    void givenDuplicateContractProduct_whenPersist_thenThrowConstraintViolation() {
        User artistUser = createUser("artist2@example.com", "작가2", Role.ARTIST, "010-2222-2222", null);
        User shopUser = createUser("shop2@example.com", "상점2", Role.SHOP, "010-8888-8888", null);
        Artist artist = createArtist(artistUser, "작가 상호2");
        Shop shop = createShop(shopUser, "입점 상점2");
        ShopArtistContract contract = createContract(artist, shop, ContractStatus.APPROVED);
        Product product = createProduct(artist, "유리 화병");

        contractProductRepository.save(ContractProduct.builder()
                .shopArtistContract(contract)
                .product(product)
                .sellingPrice(DEFAULT_SELLING_PRICE)
                .build());
        flushAndClear();

        ContractProduct duplicate = ContractProduct.builder()
                .shopArtistContract(contract)
                .product(product)
                .sellingPrice(DEFAULT_SELLING_PRICE)
                .build();

        assertThatThrownBy(() -> {
            contractProductRepository.saveAndFlush(duplicate);
            entityManager.clear();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("입고와 판매 차감 이력을 적용하면 현재 재고 스냅샷과 movement 결과가 일치한다")
    void givenInboundAndSale_whenApplyMovements_thenKeepSnapshotAndMovementConsistent() {
        ContractProduct contractProduct = createPersistedContractProduct("artist3@example.com", "shop3@example.com", "엽서 세트");

        ContractProductStockMovement inboundMovement = contractProduct.applyInbound(
                INITIAL_STOCK_QUANTITY,
                INBOUND_AT,
                ACTOR_USER_ID,
                "1차 입고"
        );
        ContractProductStockMovement saleMovement = contractProduct.applySaleDecrease(
                SALE_QUANTITY,
                SALE_AT,
                ACTOR_USER_ID,
                "판매 차감"
        );

        contractProductRepository.saveAndFlush(contractProduct);
        contractProductStockMovementRepository.save(inboundMovement);
        contractProductStockMovementRepository.save(saleMovement);
        flushAndClear();

        ContractProduct found = contractProductRepository.findById(contractProduct.getContractProductsId()).orElseThrow();
        List<ContractProductStockMovement> movements = contractProductStockMovementRepository.findAll();

        assertThat(found.getStockQuantity()).isEqualTo(INITIAL_STOCK_QUANTITY - SALE_QUANTITY);
        assertThat(found.getSoldQuantity()).isEqualTo(SALE_QUANTITY);
        assertThat(found.getRecentStockedAt()).isEqualTo(INBOUND_AT);
        assertThat(movements).hasSize(2);
        assertThat(movements.get(0).getResultStockQuantity()).isEqualTo(INITIAL_STOCK_QUANTITY);
        assertThat(movements.get(1).getQuantityDelta()).isEqualTo(-SALE_QUANTITY);
        assertThat(movements.get(1).getResultStockQuantity()).isEqualTo(INITIAL_STOCK_QUANTITY - SALE_QUANTITY);
    }

    @Test
    @DisplayName("반품 증가와 재고 조정은 최근입고일을 바꾸지 않는다")
    void givenReturnAndAdjustment_whenApplyMovements_thenKeepRecentStockedAt() {
        ContractProduct contractProduct = createPersistedContractProduct("artist4@example.com", "shop4@example.com", "마스킹 테이프");
        contractProduct.applyInbound(INITIAL_STOCK_QUANTITY, INBOUND_AT, ACTOR_USER_ID, "1차 입고");

        ContractProductStockMovement returnMovement = contractProduct.applyReturnIncrease(
                RETURN_QUANTITY,
                RETURN_AT,
                ACTOR_USER_ID,
                "반품 복원"
        );
        ContractProductStockMovement adjustmentMovement = contractProduct.applyAdjustmentDecrease(
                ADJUSTMENT_QUANTITY,
                ADJUSTMENT_AT,
                ACTOR_USER_ID,
                "재고 조정"
        );

        contractProductRepository.saveAndFlush(contractProduct);
        contractProductStockMovementRepository.save(returnMovement);
        contractProductStockMovementRepository.save(adjustmentMovement);
        flushAndClear();

        ContractProduct found = contractProductRepository.findById(contractProduct.getContractProductsId()).orElseThrow();

        assertThat(found.getRecentStockedAt()).isEqualTo(INBOUND_AT);
        assertThat(found.getStockQuantity()).isEqualTo(INITIAL_STOCK_QUANTITY + RETURN_QUANTITY - ADJUSTMENT_QUANTITY);
    }

    @Test
    @DisplayName("현재 재고보다 많이 차감하면 예외가 발생한다")
    void givenInsufficientStock_whenApplySaleDecrease_thenThrowInvalidInputException() {
        ContractProduct contractProduct = createPersistedContractProduct("artist5@example.com", "shop5@example.com", "패브릭 파우치");
        contractProduct.applyInbound(INITIAL_STOCK_QUANTITY, INBOUND_AT, ACTOR_USER_ID, "1차 입고");

        assertThatThrownBy(() -> contractProduct.applySaleDecrease(
                INITIAL_STOCK_QUANTITY + OVERSELL_EXTRA_QUANTITY,
                SALE_AT,
                ACTOR_USER_ID,
                "과다 차감"
        )).isInstanceOf(InvalidInputException.class);
    }

    @Test
    @DisplayName("재고관리 목록 조회 시 승인 계약 작가는 품목이 없어도 반환한다")
    void givenApprovedContracts_whenQueryByShop_thenReturnContractedArtists() {
        User artistUser = createUser("artist6@example.com", "작가6", Role.ARTIST, "010-6666-6666", "https://image.test/artist6.png");
        User anotherArtistUser = createUser("artist7@example.com", "작가7", Role.ARTIST, "010-7777-7777", "https://image.test/artist7.png");
        User noProductArtistUser = createUser("artist8@example.com", "작가8", Role.ARTIST, "010-7777-7778", "https://image.test/artist8.png");
        User shopUser = createUser("shop6@example.com", "상점6", Role.SHOP, "010-5555-5555", null);
        Artist activeArtist = createArtist(artistUser, "활성 작가");
        Artist endedArtist = createArtist(anotherArtistUser, "종료 작가");
        Artist noProductArtist = createArtist(noProductArtistUser, "무품목 작가");
        Shop shop = createShop(shopUser, "조회 상점");
        ShopArtistContract activeContract = createContract(activeArtist, shop, ContractStatus.APPROVED);
        ShopArtistContract endedContract = createContract(endedArtist, shop, ContractStatus.ENDED);
        ShopArtistContract noProductContract = createContract(noProductArtist, shop, ContractStatus.APPROVED);
        Product activeProduct = createProduct(activeArtist, "도자기 접시");
        Product endedProduct = createProduct(endedArtist, "종료 품목");

        ContractProduct activeContractProduct = ContractProduct.builder()
                .shopArtistContract(activeContract)
                .product(activeProduct)
                .sellingPrice(DEFAULT_SELLING_PRICE)
                .build();
        activeContractProduct.applyInbound(INITIAL_STOCK_QUANTITY, INBOUND_AT, ACTOR_USER_ID, "입고");

        ContractProduct endedContractProduct = ContractProduct.builder()
                .shopArtistContract(endedContract)
                .product(endedProduct)
                .sellingPrice(DEFAULT_SELLING_PRICE)
                .listingStatus(ContractProductListingStatus.ENDED)
                .build();
        endedContractProduct.applyInbound(INITIAL_STOCK_QUANTITY, BASE_STOCKED_AT, ACTOR_USER_ID, "종료 입고");

        contractProductRepository.save(activeContractProduct);
        contractProductRepository.save(endedContractProduct);
        flushAndClear();

        List<ContractInventoryRow> rows = contractRepository.findInventoryRowsByShopId(
                shop.getShopId(),
                ContractStatus.APPROVED,
                ContractProductListingStatus.ACTIVE
        );

        assertThat(rows).hasSize(2);
        ContractInventoryRow row = rows.stream()
                .filter(inventoryRow -> "활성 작가".equals(inventoryRow.getArtistName()))
                .findFirst()
                .orElseThrow();
        ContractInventoryRow noProductRow = rows.stream()
                .filter(inventoryRow -> "무품목 작가".equals(inventoryRow.getArtistName()))
                .findFirst()
                .orElseThrow();

        assertThat(row.getContractId()).isEqualTo(activeContract.getShopArtistContractsId());
        assertThat(row.getArtistImageUrl()).isEqualTo("https://image.test/artist6.png");
        assertThat(row.getArtistName()).isEqualTo("활성 작가");
        assertThat(row.getSpecialty()).isEqualTo(DEFAULT_SPECIALTY);
        assertThat(row.getRecentStockedAt()).isEqualTo(INBOUND_AT);
        assertThat(row.getInboundConfirmed()).isFalse();
        assertThat(noProductRow.getContractId()).isEqualTo(noProductContract.getShopArtistContractsId());
        assertThat(noProductRow.getRecentStockedAt()).isNull();
    }

    @Test
    @DisplayName("작가별 재고 상세 조회 시 총 입고 수량과 정산 필드를 반환한다")
    void givenContractProducts_whenQueryDetailRows_thenReturnInventoryProductRows() {
        User artistUser = createUser("artist9@example.com", "작가9", Role.ARTIST, "010-9999-0001", "https://image.test/artist9.png");
        User shopUser = createUser("shop9@example.com", "상점9", Role.SHOP, "010-9999-0002", null);
        Artist artist = createArtist(artistUser, "상세 작가");
        Shop shop = createShop(shopUser, "상세 상점");
        ShopArtistContract contract = createContract(artist, shop, ContractStatus.APPROVED);
        Product product = createProduct(artist, "유리 컵");

        ContractProduct contractProduct = ContractProduct.builder()
                .shopArtistContract(contract)
                .product(product)
                .sellingPrice(DEFAULT_SELLING_PRICE)
                .build();
        contractProduct.updateInventoryManagement(UPDATED_MARGIN_AMOUNT, UPDATED_UNIT_SETTLEMENT_AMOUNT);
        ContractProductStockMovement firstInbound = contractProduct.applyInbound(
                INITIAL_STOCK_QUANTITY,
                INBOUND_AT,
                ACTOR_USER_ID,
                "1차 입고"
        );
        ContractProductStockMovement secondInbound = contractProduct.applyInbound(
                INBOUND_QUANTITY,
                ADJUSTMENT_AT,
                ACTOR_USER_ID,
                "2차 입고"
        );
        ContractProductStockMovement sale = contractProduct.applySaleDecrease(
                SALE_QUANTITY,
                SALE_AT,
                ACTOR_USER_ID,
                "판매"
        );

        contractProductRepository.save(contractProduct);
        contractProductStockMovementRepository.save(firstInbound);
        contractProductStockMovementRepository.save(secondInbound);
        contractProductStockMovementRepository.save(sale);
        flushAndClear();

        List<ContractInventoryProductRow> rows = contractProductRepository.findInventoryProductRowsByContractId(
                contract.getShopArtistContractsId(),
                shop.getShopId(),
                ContractStatus.APPROVED,
                ContractProductListingStatus.ACTIVE,
                ContractProductStockMovementType.INBOUND
        );

        assertThat(rows).hasSize(1);
        ContractInventoryProductRow row = rows.get(0);
        assertThat(row.getTotalQuantity()).isEqualTo((long) INITIAL_STOCK_QUANTITY + INBOUND_QUANTITY);
        assertThat(row.getProductImageUrl()).isEqualTo("https://example.com/products/유리 컵");
        assertThat(row.getProductName()).isEqualTo("유리 컵");
        assertThat(row.getSellingPrice()).isEqualByComparingTo(DEFAULT_SELLING_PRICE);
        assertThat(row.getStockQuantity()).isEqualTo(INITIAL_STOCK_QUANTITY + INBOUND_QUANTITY - SALE_QUANTITY);
        assertThat(row.getCommissionType()).isEqualTo(CommissionType.RATE);
        assertThat(row.getCommissionValue()).isEqualByComparingTo(DEFAULT_COMMISSION_VALUE);
        assertThat(row.getMarginAmount()).isEqualByComparingTo(UPDATED_MARGIN_AMOUNT);
        assertThat(row.getUnitSettlementAmount()).isEqualByComparingTo(UPDATED_UNIT_SETTLEMENT_AMOUNT);
        assertThat(row.getArtistName()).isEqualTo("상세 작가");
        assertThat(row.getRecentStockedAt()).isEqualTo(ADJUSTMENT_AT);
    }

    private ContractProduct createPersistedContractProduct(String artistEmail, String shopEmail, String productName) {
        User artistUser = createUser(artistEmail, artistEmail, Role.ARTIST, uniquePhoneFor(artistEmail), null);
        User shopUser = createUser(shopEmail, shopEmail, Role.SHOP, uniquePhoneFor(shopEmail), null);
        Artist artist = createArtist(artistUser, productName + " 작가");
        Shop shop = createShop(shopUser, productName + " 상점");
        ShopArtistContract contract = createContract(artist, shop, ContractStatus.APPROVED);
        Product product = createProduct(artist, productName);

        ContractProduct contractProduct = ContractProduct.builder()
                .shopArtistContract(contract)
                .product(product)
                .sellingPrice(DEFAULT_SELLING_PRICE)
                .build();

        return contractProductRepository.saveAndFlush(contractProduct);
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

    private Artist createArtist(User user, String businessName) {
        BusinessProfile businessProfile = createBusinessProfile(user, "ART-" + user.getId(), businessName, "서울시 성동구");
        Artist artist = Artist.builder()
                .user(user)
                .businessProfile(businessProfile)
                .build();
        return artistRepository.save(artist);
    }

    private Shop createShop(User user, String shopName) {
        BusinessProfile businessProfile = createBusinessProfile(user, "SHOP-" + user.getId(), shopName, "서울시 마포구");
        Shop shop = Shop.builder()
                .user(user)
                .businessProfile(businessProfile)
                .shopName(shopName)
                .shopDescription(shopName + " 설명")
                .build();
        return shopRepository.save(shop);
    }

    private BusinessProfile createBusinessProfile(
            User user,
            String businessNumber,
            String businessName,
            String businessAddress
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
                .specialty(DEFAULT_SPECIALTY)
                .reviewDataAgreement(Boolean.TRUE)
                .build();
        return businessProfileRepository.save(businessProfile);
    }

    private ShopArtistContract createContract(Artist artist, Shop shop, ContractStatus contractStatus) {
        ShopArtistContract contract = ShopArtistContract.builder()
                .artist(artist)
                .shop(shop)
                .contractStatus(contractStatus)
                .commissionType(CommissionType.RATE)
                .commissionValue(DEFAULT_COMMISSION_VALUE)
                .build();
        return contractRepository.save(contract);
    }

    private Product createProduct(Artist artist, String productName) {
        Product product = Product.builder()
                .artist(artist)
                .productName(productName)
                .price(DEFAULT_PRODUCT_PRICE)
                .status(ProductStatus.ACTIVE)
                .productUrl("https://example.com/products/" + productName)
                .build();
        entityManager.persist(product);
        return product;
    }

    private String uniquePhoneFor(String seed) {
        int hash = Math.floorMod(seed.hashCode(), 100_000_000);
        return "010" + String.format("%08d", hash);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
