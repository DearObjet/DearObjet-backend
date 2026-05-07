package app.dearobjet.backend.domain.artist;

import app.dearobjet.backend.domain.artist.dto.ArtistProductListResponse;
import app.dearobjet.backend.domain.artist.dto.CreateArtistProductRequest;
import app.dearobjet.backend.domain.artist.dto.UpdateArtistProductRequest;
import app.dearobjet.backend.domain.artist.dto.UpdateArtistProductStockRequest;
import app.dearobjet.backend.domain.artist.dto.UpdateArtistProductStockResponse;
import app.dearobjet.backend.domain.artist.entity.Artist;
import app.dearobjet.backend.domain.artist.service.ArtistProductService;
import app.dearobjet.backend.domain.shop.entity.Product;
import app.dearobjet.backend.domain.shop.enums.ProductStatus;
import app.dearobjet.backend.domain.shop.repository.ProductRepository;
import app.dearobjet.backend.domain.user.repository.ArtistRepository;
import app.dearobjet.backend.global.exception.BusinessException;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.InvalidInputException;
import app.dearobjet.backend.global.s3.service.S3FileUploadService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@DisplayName("ArtistProductService 품목 및 재고관리 테스트")
@ExtendWith(MockitoExtension.class)
class ArtistProductServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long ARTIST_ID = 10L;
    private static final Long CUP_PRODUCT_ID = 100L;
    private static final Long PLATE_PRODUCT_ID = 101L;
    private static final Long VERSION = 0L;
    private static final int PAGE = 1;
    private static final int SIZE = 20;
    private static final int CUP_STOCK_QUANTITY = 5;
    private static final int UPDATED_CUP_STOCK_QUANTITY = 12;
    private static final int UPDATED_PLATE_STOCK_QUANTITY = 3;
    private static final BigDecimal DEFAULT_PRICE = new BigDecimal("10000.00");
    private static final BigDecimal UPDATED_PRICE = new BigDecimal("15000.00");
    private static final String PRODUCT_IMAGE_URL = "https://image.test/products/new.png";
    private static final String UPDATED_PRODUCT_IMAGE_URL = "https://image.test/products/updated.png";

    @InjectMocks
    private ArtistProductService artistProductService;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private S3FileUploadService s3FileUploadService;

    @Test
    @DisplayName("상품 이미지와 함께 작가 상품을 등록한다")
    void givenProductRequestAndImage_whenCreateProduct_thenSaveActiveProduct() {
        Artist artist = artist();
        CreateArtistProductRequest request = createProductRequest("도자기 컵", DEFAULT_PRICE, CUP_STOCK_QUANTITY);
        MultipartFile productImage = productImage();

        given(artistRepository.findByUserId(USER_ID)).willReturn(Optional.of(artist));
        given(s3FileUploadService.uploadProductImage(productImage, USER_ID)).willReturn(PRODUCT_IMAGE_URL);
        given(productRepository.save(any(Product.class))).willAnswer(invocation -> {
            Product savedProduct = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedProduct, "productsId", CUP_PRODUCT_ID);
            ReflectionTestUtils.setField(savedProduct, "version", VERSION);
            return savedProduct;
        });

        var response = artistProductService.createProduct(USER_ID, request, productImage);

        assertThat(response.getProductId()).isEqualTo(CUP_PRODUCT_ID);
        assertThat(response.getProductName()).isEqualTo("도자기 컵");
        assertThat(response.getPrice()).isEqualByComparingTo(DEFAULT_PRICE);
        assertThat(response.getStockQuantity()).isEqualTo(CUP_STOCK_QUANTITY);
        assertThat(response.getImageUrl()).isEqualTo(PRODUCT_IMAGE_URL);
    }

    @Test
    @DisplayName("상품 수정 시 이미지가 없으면 기존 이미지를 유지한다")
    void givenUpdateRequestWithoutImage_whenUpdateProduct_thenKeepImage() {
        Artist artist = artist();
        Product product = product(CUP_PRODUCT_ID, "도자기 컵", CUP_STOCK_QUANTITY, VERSION);
        UpdateArtistProductRequest request = updateProductRequest("수정 컵", UPDATED_PRICE, UPDATED_CUP_STOCK_QUANTITY);

        given(artistRepository.findByUserId(USER_ID)).willReturn(Optional.of(artist));
        given(productRepository.findByProductsIdAndArtistIdAndStatus(
                CUP_PRODUCT_ID,
                ARTIST_ID,
                ProductStatus.ACTIVE
        )).willReturn(Optional.of(product));

        var response = artistProductService.updateProduct(USER_ID, CUP_PRODUCT_ID, request, null);

        assertThat(response.getProductName()).isEqualTo("수정 컵");
        assertThat(response.getPrice()).isEqualByComparingTo(UPDATED_PRICE);
        assertThat(response.getStockQuantity()).isEqualTo(UPDATED_CUP_STOCK_QUANTITY);
        assertThat(response.getImageUrl()).isEqualTo("https://image.test/products/" + CUP_PRODUCT_ID + ".png");
    }

    @Test
    @DisplayName("상품 수정 시 새 이미지가 있으면 이미지 URL을 교체한다")
    void givenUpdateRequestWithImage_whenUpdateProduct_thenReplaceImage() {
        Artist artist = artist();
        Product product = product(CUP_PRODUCT_ID, "도자기 컵", CUP_STOCK_QUANTITY, VERSION);
        UpdateArtistProductRequest request = updateProductRequest("수정 컵", UPDATED_PRICE, UPDATED_CUP_STOCK_QUANTITY);
        MultipartFile productImage = productImage();

        given(artistRepository.findByUserId(USER_ID)).willReturn(Optional.of(artist));
        given(productRepository.findByProductsIdAndArtistIdAndStatus(
                CUP_PRODUCT_ID,
                ARTIST_ID,
                ProductStatus.ACTIVE
        )).willReturn(Optional.of(product));
        given(s3FileUploadService.uploadProductImage(productImage, USER_ID)).willReturn(UPDATED_PRODUCT_IMAGE_URL);

        var response = artistProductService.updateProduct(USER_ID, CUP_PRODUCT_ID, request, productImage);

        assertThat(response.getImageUrl()).isEqualTo(UPDATED_PRODUCT_IMAGE_URL);
        assertThat(product.getProductUrl()).isEqualTo(UPDATED_PRODUCT_IMAGE_URL);
    }

    @Test
    @DisplayName("다른 작가 상품은 수정할 수 없다")
    void givenNotOwnedProduct_whenUpdateProduct_thenThrowNotFound() {
        Artist artist = artist();
        UpdateArtistProductRequest request = updateProductRequest("수정 컵", UPDATED_PRICE, UPDATED_CUP_STOCK_QUANTITY);

        given(artistRepository.findByUserId(USER_ID)).willReturn(Optional.of(artist));
        given(productRepository.findByProductsIdAndArtistIdAndStatus(
                CUP_PRODUCT_ID,
                ARTIST_ID,
                ProductStatus.ACTIVE
        )).willReturn(Optional.empty());

        assertThatThrownBy(() -> artistProductService.updateProduct(USER_ID, CUP_PRODUCT_ID, request, null))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("상품을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("작가의 활성 상품 목록을 조회한다")
    void givenArtist_whenGetProducts_thenReturnActiveProducts() {
        Artist artist = artist();
        Product product = product(CUP_PRODUCT_ID, "도자기 컵", CUP_STOCK_QUANTITY, VERSION);

        given(artistRepository.findByUserId(USER_ID)).willReturn(Optional.of(artist));
        given(productRepository.findByArtistIdAndStatus(
                ARTIST_ID,
                ProductStatus.ACTIVE,
                PageRequest.of(0, SIZE, org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC,
                        "createdAt"
                ).and(org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC,
                        "productsId"
                )))
        )).willReturn(new PageImpl<>(List.of(product)));

        ArtistProductListResponse response = artistProductService.getProducts(USER_ID, PAGE, SIZE);

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getProductId()).isEqualTo(CUP_PRODUCT_ID);
        assertThat(response.getItems().get(0).getStockQuantity()).isEqualTo(CUP_STOCK_QUANTITY);
    }

    @Test
    @DisplayName("체크한 상품들의 재고를 요청 순서대로 일괄 변경한다")
    void givenStockRequest_whenUpdateStocks_thenUpdateAllProducts() {
        Artist artist = artist();
        Product cup = product(CUP_PRODUCT_ID, "도자기 컵", CUP_STOCK_QUANTITY, VERSION);
        Product plate = product(PLATE_PRODUCT_ID, "디저트 접시", 0, VERSION);
        UpdateArtistProductStockRequest request = stockRequest(
                stockItem(CUP_PRODUCT_ID, UPDATED_CUP_STOCK_QUANTITY, VERSION),
                stockItem(PLATE_PRODUCT_ID, UPDATED_PLATE_STOCK_QUANTITY, VERSION)
        );

        given(artistRepository.findByUserId(USER_ID)).willReturn(Optional.of(artist));
        given(productRepository.findByProductsIdInAndArtistIdAndStatus(
                List.of(CUP_PRODUCT_ID, PLATE_PRODUCT_ID),
                ARTIST_ID,
                ProductStatus.ACTIVE
        )).willReturn(List.of(plate, cup));

        UpdateArtistProductStockResponse response = artistProductService.updateStocks(USER_ID, request);

        assertThat(cup.getStockQuantity()).isEqualTo(UPDATED_CUP_STOCK_QUANTITY);
        assertThat(plate.getStockQuantity()).isEqualTo(UPDATED_PLATE_STOCK_QUANTITY);
        assertThat(response.getItems())
                .extracting("productId")
                .containsExactly(CUP_PRODUCT_ID, PLATE_PRODUCT_ID);
        verify(productRepository).flush();
    }

    @Test
    @DisplayName("상품 버전이 다르면 재고 변경을 거부한다")
    void givenStaleVersion_whenUpdateStocks_thenThrowConflict() {
        Artist artist = artist();
        Product product = product(CUP_PRODUCT_ID, "도자기 컵", CUP_STOCK_QUANTITY, VERSION);
        UpdateArtistProductStockRequest request = stockRequest(
                stockItem(CUP_PRODUCT_ID, UPDATED_CUP_STOCK_QUANTITY, 99L)
        );

        given(artistRepository.findByUserId(USER_ID)).willReturn(Optional.of(artist));
        given(productRepository.findByProductsIdInAndArtistIdAndStatus(
                List.of(CUP_PRODUCT_ID),
                ARTIST_ID,
                ProductStatus.ACTIVE
        )).willReturn(List.of(product));

        assertThatThrownBy(() -> artistProductService.updateStocks(USER_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("상품 재고가 이미 변경되었습니다");

        assertThat(product.getStockQuantity()).isEqualTo(CUP_STOCK_QUANTITY);
    }

    @Test
    @DisplayName("다른 작가 상품이 포함되면 재고 변경을 거부한다")
    void givenNotOwnedProduct_whenUpdateStocks_thenThrowNotFound() {
        Artist artist = artist();
        UpdateArtistProductStockRequest request = stockRequest(
                stockItem(CUP_PRODUCT_ID, UPDATED_CUP_STOCK_QUANTITY, VERSION)
        );

        given(artistRepository.findByUserId(USER_ID)).willReturn(Optional.of(artist));
        given(productRepository.findByProductsIdInAndArtistIdAndStatus(
                List.of(CUP_PRODUCT_ID),
                ARTIST_ID,
                ProductStatus.ACTIVE
        )).willReturn(List.of());

        assertThatThrownBy(() -> artistProductService.updateStocks(USER_ID, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("상품을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("중복 상품 ID가 포함되면 재고 변경을 거부한다")
    void givenDuplicateProductId_whenUpdateStocks_thenThrowInvalidInput() {
        Artist artist = artist();
        UpdateArtistProductStockRequest request = stockRequest(
                stockItem(CUP_PRODUCT_ID, UPDATED_CUP_STOCK_QUANTITY, VERSION),
                stockItem(CUP_PRODUCT_ID, UPDATED_PLATE_STOCK_QUANTITY, VERSION)
        );

        given(artistRepository.findByUserId(USER_ID)).willReturn(Optional.of(artist));

        assertThatThrownBy(() -> artistProductService.updateStocks(USER_ID, request))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("중복된 상품 ID가 포함되어 있습니다.");
    }

    @Test
    @DisplayName("작가 상품 삭제는 비활성화로 처리한다")
    void givenOwnedProduct_whenDeleteProduct_thenDeactivate() {
        Artist artist = artist();
        Product product = product(CUP_PRODUCT_ID, "도자기 컵", CUP_STOCK_QUANTITY, VERSION);

        given(artistRepository.findByUserId(USER_ID)).willReturn(Optional.of(artist));
        given(productRepository.findByProductsIdAndArtistIdAndStatus(
                CUP_PRODUCT_ID,
                ARTIST_ID,
                ProductStatus.ACTIVE
        )).willReturn(Optional.of(product));

        artistProductService.deleteProduct(USER_ID, CUP_PRODUCT_ID);

        assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
    }

    @Test
    @DisplayName("페이지 크기가 1보다 작으면 목록 조회를 거부한다")
    void givenInvalidSize_whenGetProducts_thenThrowInvalidInput() {
        assertThatThrownBy(() -> artistProductService.getProducts(USER_ID, PAGE, 0))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("페이지 크기는 1 이상이어야 합니다.");
    }

    private Artist artist() {
        return Artist.builder()
                .id(ARTIST_ID)
                .build();
    }

    private Product product(Long productId, String productName, int stockQuantity, Long version) {
        return Product.builder()
                .productsId(productId)
                .artist(artist())
                .productName(productName)
                .price(new BigDecimal("10000.00"))
                .status(ProductStatus.ACTIVE)
                .productUrl("https://image.test/products/" + productId + ".png")
                .stockQuantity(stockQuantity)
                .version(version)
                .build();
    }

    private CreateArtistProductRequest createProductRequest(
            String productName,
            BigDecimal price,
            int stockQuantity
    ) {
        CreateArtistProductRequest request = new CreateArtistProductRequest();
        ReflectionTestUtils.setField(request, "productName", productName);
        ReflectionTestUtils.setField(request, "price", price);
        ReflectionTestUtils.setField(request, "stockQuantity", stockQuantity);
        return request;
    }

    private UpdateArtistProductRequest updateProductRequest(
            String productName,
            BigDecimal price,
            int stockQuantity
    ) {
        UpdateArtistProductRequest request = new UpdateArtistProductRequest();
        ReflectionTestUtils.setField(request, "productName", productName);
        ReflectionTestUtils.setField(request, "price", price);
        ReflectionTestUtils.setField(request, "stockQuantity", stockQuantity);
        return request;
    }

    private UpdateArtistProductStockRequest stockRequest(UpdateArtistProductStockRequest.Item... items) {
        UpdateArtistProductStockRequest request = new UpdateArtistProductStockRequest();
        ReflectionTestUtils.setField(request, "items", List.of(items));
        return request;
    }

    private UpdateArtistProductStockRequest.Item stockItem(
            Long productId,
            int stockQuantity,
            Long version
    ) {
        UpdateArtistProductStockRequest.Item item = new UpdateArtistProductStockRequest.Item();
        ReflectionTestUtils.setField(item, "productId", productId);
        ReflectionTestUtils.setField(item, "stockQuantity", stockQuantity);
        ReflectionTestUtils.setField(item, "version", version);
        return item;
    }

    private MultipartFile productImage() {
        return new MockMultipartFile(
                "productImage",
                "product.png",
                "image/png",
                "image".getBytes()
        );
    }
}
