package app.dearobjet.backend.domain.artist;

import app.dearobjet.backend.domain.artist.entity.Artist;
import app.dearobjet.backend.domain.shop.entity.Product;
import app.dearobjet.backend.domain.shop.enums.ProductStatus;
import app.dearobjet.backend.domain.shop.repository.ProductRepository;
import app.dearobjet.backend.domain.user.entity.BusinessProfile;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.enums.BusinessCategory;
import app.dearobjet.backend.domain.user.enums.BusinessType;
import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.enums.Specialty;
import app.dearobjet.backend.domain.user.enums.UserStatus;
import app.dearobjet.backend.domain.user.repository.ArtistRepository;
import app.dearobjet.backend.domain.user.repository.BusinessProfileRepository;
import app.dearobjet.backend.domain.user.repository.UserRepository;
import app.dearobjet.backend.global.common.config.JpaConfig;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
class ProductRepositoryTest {

    private static final BigDecimal DEFAULT_PRICE = new BigDecimal("10000.00");
    private static final int FIRST_PAGE_INDEX = 0;
    private static final int PAGE_SIZE = 10;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private BusinessProfileRepository businessProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("작가 상품 목록은 활성 상품만 최신 등록순으로 조회한다")
    void givenProducts_whenFindByArtist_thenReturnActiveProductsByNewest() {
        Artist artist = createArtist("artist-a@test.com", "Postman 도자기 작가");
        Artist otherArtist = createArtist("artist-b@test.com", "Postman 문구 작가");
        Product oldProduct = createProduct(artist, "오래된 컵", ProductStatus.ACTIVE);
        Product newestProduct = createProduct(artist, "최신 접시", ProductStatus.ACTIVE);
        createProduct(artist, "비활성 화병", ProductStatus.INACTIVE);
        createProduct(otherArtist, "다른 작가 상품", ProductStatus.ACTIVE);

        Page<Product> result = productRepository.findByArtistIdAndStatus(
                artist.getId(),
                ProductStatus.ACTIVE,
                PageRequest.of(
                        FIRST_PAGE_INDEX,
                        PAGE_SIZE,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                                .and(Sort.by(Sort.Direction.DESC, "productsId"))
                )
        );

        assertThat(result.getContent())
                .extracting(Product::getProductName)
                .containsExactly("최신 접시", "오래된 컵");
        assertThat(result.getContent())
                .extracting(Product::getProductsId)
                .containsExactly(newestProduct.getProductsId(), oldProduct.getProductsId());
    }

    @Test
    @DisplayName("작가 소유 활성 상품만 ID 목록으로 조회한다")
    void givenProductIds_whenFindByIdsAndArtist_thenReturnOnlyOwnedActiveProducts() {
        Artist artist = createArtist("artist-a@test.com", "Postman 도자기 작가");
        Artist otherArtist = createArtist("artist-b@test.com", "Postman 문구 작가");
        Product activeProduct = createProduct(artist, "활성 컵", ProductStatus.ACTIVE);
        Product inactiveProduct = createProduct(artist, "비활성 접시", ProductStatus.INACTIVE);
        Product otherArtistProduct = createProduct(otherArtist, "다른 작가 상품", ProductStatus.ACTIVE);

        List<Product> products = productRepository.findByProductsIdInAndArtistIdAndStatus(
                List.of(
                        activeProduct.getProductsId(),
                        inactiveProduct.getProductsId(),
                        otherArtistProduct.getProductsId()
                ),
                artist.getId(),
                ProductStatus.ACTIVE
        );

        assertThat(products)
                .extracting(Product::getProductsId)
                .containsExactly(activeProduct.getProductsId());
    }

    @Test
    @DisplayName("출고 가능 상품은 작가 보유 재고가 있고 상품명 검색어에 맞는 활성 상품만 조회한다")
    void givenKeyword_whenFindShipmentAvailableProducts_thenReturnMatchedStockProducts() {
        Artist artist = createArtist("artist-c@test.com", "Postman 출고 작가");
        Artist otherArtist = createArtist("artist-d@test.com", "Postman 다른 출고 작가");
        Product cup = createProduct(artist, "세라믹 컵", ProductStatus.ACTIVE, 5);
        createProduct(artist, "세라믹 접시", ProductStatus.ACTIVE, 0);
        createProduct(artist, "비활성 컵", ProductStatus.INACTIVE, 5);
        createProduct(otherArtist, "다른 작가 컵", ProductStatus.ACTIVE, 5);

        Page<Product> products = productRepository.findShipmentAvailableProducts(
                artist.getId(),
                ProductStatus.ACTIVE,
                "컵",
                PageRequest.of(FIRST_PAGE_INDEX, PAGE_SIZE)
        );

        assertThat(products.getContent())
                .extracting(Product::getProductsId)
                .containsExactly(cup.getProductsId());
    }

    @Test
    @DisplayName("상품 메모를 저장하고 조회한다")
    void givenProductMemo_whenSaveAndFind_thenReturnMemo() {
        Artist artist = createArtist("artist-a@test.com", "Postman 도자기 작가");
        Product product = createProduct(artist, "활성 컵", ProductStatus.ACTIVE);
        product.updateMemo("매장 전면 진열");
        productRepository.flush();

        Product foundProduct = productRepository.findById(product.getProductsId()).orElseThrow();

        assertThat(foundProduct.getMemo()).isEqualTo("매장 전면 진열");
    }

    private Artist createArtist(String email, String businessName) {
        User user = userRepository.save(User.builder()
                .email(email)
                .name(businessName)
                .role(Role.ARTIST)
                .userStatus(UserStatus.ACTIVE)
                .build());
        BusinessProfile businessProfile = businessProfileRepository.save(BusinessProfile.builder()
                .user(user)
                .businessType(BusinessType.WHOLESALE_RETAIL)
                .businessNumber("123-45-" + Math.abs(email.hashCode() % 100000))
                .businessName(businessName)
                .ownerName("대표")
                .businessAddress("서울시 성동구")
                .businessCategory(BusinessCategory.CRAFT_RETAIL)
                .specialty(Specialty.CERAMIC)
                .reviewDataAgreement(true)
                .build());
        return artistRepository.save(Artist.builder()
                .user(user)
                .businessProfile(businessProfile)
                .build());
    }

    private Product createProduct(Artist artist, String productName, ProductStatus status) {
        return createProduct(artist, productName, status, 0);
    }

    private Product createProduct(Artist artist, String productName, ProductStatus status, int stockQuantity) {
        return productRepository.save(Product.builder()
                .artist(artist)
                .productName(productName)
                .price(DEFAULT_PRICE)
                .status(status)
                .productUrl("https://image.test/products/" + productName + ".png")
                .stockQuantity(stockQuantity)
                .build());
    }
}
