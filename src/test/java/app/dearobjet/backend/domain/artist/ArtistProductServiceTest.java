package app.dearobjet.backend.domain.artist;

import app.dearobjet.backend.domain.artist.dto.ArtistProductListResponse;
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
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @InjectMocks
    private ArtistProductService artistProductService;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private ProductRepository productRepository;

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
}
