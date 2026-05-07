package app.dearobjet.backend.domain.artist.service;

import app.dearobjet.backend.domain.artist.dto.ArtistProductItemResponse;
import app.dearobjet.backend.domain.artist.dto.ArtistProductListResponse;
import app.dearobjet.backend.domain.artist.dto.ArtistProductMemoResponse;
import app.dearobjet.backend.domain.artist.dto.CreateArtistProductRequest;
import app.dearobjet.backend.domain.artist.dto.UpdateArtistProductMemoRequest;
import app.dearobjet.backend.domain.artist.dto.UpdateArtistProductRequest;
import app.dearobjet.backend.domain.artist.dto.UpdateArtistProductStockRequest;
import app.dearobjet.backend.domain.artist.dto.UpdateArtistProductStockResponse;
import app.dearobjet.backend.domain.artist.entity.Artist;
import app.dearobjet.backend.domain.shop.entity.Product;
import app.dearobjet.backend.domain.shop.enums.ProductStatus;
import app.dearobjet.backend.domain.shop.repository.ProductRepository;
import app.dearobjet.backend.domain.user.repository.ArtistRepository;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.ErrorCode;
import app.dearobjet.backend.global.exception.InvalidInputException;
import app.dearobjet.backend.global.s3.service.S3FileUploadService;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ArtistProductService {

    private static final int FIRST_PAGE = 1;
    private static final int FIRST_PAGE_INDEX = 0;
    private static final int MIN_PAGE_SIZE = 1;

    private final ArtistRepository artistRepository;
    private final ProductRepository productRepository;
    private final S3FileUploadService s3FileUploadService;

    @Transactional
    public ArtistProductItemResponse createProduct(
            Long userId,
            CreateArtistProductRequest request,
            MultipartFile productImage
    ) {
        Artist artist = getArtistByUserId(userId);
        String productImageUrl = s3FileUploadService.uploadProductImage(productImage, userId);

        Product product = Product.create(
                artist,
                request.getProductName(),
                request.getPrice(),
                request.getStockQuantity(),
                productImageUrl
        );

        return ArtistProductItemResponse.from(productRepository.save(product));
    }

    @Transactional
    public ArtistProductItemResponse updateProduct(
            Long userId,
            Long productId,
            UpdateArtistProductRequest request,
            MultipartFile productImage
    ) {
        Artist artist = getArtistByUserId(userId);
        Product product = getOwnedActiveProduct(productId, artist.getId());

        if (hasImageFile(productImage)) {
            String productImageUrl = s3FileUploadService.uploadProductImage(productImage, userId);
            product.updateProductInfo(
                    request.getProductName(),
                    request.getPrice(),
                    request.getStockQuantity(),
                    productImageUrl
            );
            return ArtistProductItemResponse.from(product);
        }

        product.updateProductInfoKeepingImage(
                request.getProductName(),
                request.getPrice(),
                request.getStockQuantity()
        );
        return ArtistProductItemResponse.from(product);
    }

    @Transactional(readOnly = true)
    public ArtistProductListResponse getProducts(Long userId, int page, int size) {
        validatePageSize(size);
        Artist artist = getArtistByUserId(userId);
        PageRequest pageRequest = PageRequest.of(
                toPageIndex(page),
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "productsId"))
        );

        Page<Product> productPage = productRepository.findByArtistIdAndStatus(
                artist.getId(),
                ProductStatus.ACTIVE,
                pageRequest
        );

        return ArtistProductListResponse.from(productPage, page);
    }

    @Transactional
    public UpdateArtistProductStockResponse updateStocks(
            Long userId,
            UpdateArtistProductStockRequest request
    ) {
        Artist artist = getArtistByUserId(userId);
        validateDuplicateProductIds(request);

        List<Long> productIds = request.getItems().stream()
                .map(UpdateArtistProductStockRequest.Item::getProductId)
                .toList();
        Map<Long, Product> productMap = productRepository.findByProductsIdInAndArtistIdAndStatus(
                        productIds,
                        artist.getId(),
                        ProductStatus.ACTIVE
                ).stream()
                .collect(Collectors.toMap(Product::getProductsId, Function.identity()));

        if (productMap.size() != productIds.size()) {
            throw new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "상품을 찾을 수 없습니다.");
        }

        List<Product> updatedProducts = request.getItems().stream()
                .map(item -> updateStock(productMap.get(item.getProductId()), item))
                .toList();

        productRepository.flush();

        return UpdateArtistProductStockResponse.from(updatedProducts);
    }

    @Transactional(readOnly = true)
    public ArtistProductMemoResponse getProductMemo(Long userId, Long productId) {
        Artist artist = getArtistByUserId(userId);
        Product product = getOwnedActiveProduct(productId, artist.getId());

        return ArtistProductMemoResponse.from(product);
    }

    @Transactional
    public ArtistProductMemoResponse updateProductMemo(
            Long userId,
            Long productId,
            UpdateArtistProductMemoRequest request
    ) {
        Artist artist = getArtistByUserId(userId);
        Product product = getOwnedActiveProduct(productId, artist.getId());
        product.updateMemo(request.getMemo());

        return ArtistProductMemoResponse.from(product);
    }

    @Transactional
    public ArtistProductMemoResponse deleteProductMemo(Long userId, Long productId) {
        Artist artist = getArtistByUserId(userId);
        Product product = getOwnedActiveProduct(productId, artist.getId());
        product.updateMemo("");

        return ArtistProductMemoResponse.from(product);
    }

    @Transactional
    public void deleteProduct(Long userId, Long productId) {
        Artist artist = getArtistByUserId(userId);
        Product product = getOwnedActiveProduct(productId, artist.getId());

        product.deactivate();
    }

    private Product getOwnedActiveProduct(Long productId, Long artistId) {
        return productRepository.findByProductsIdAndArtistIdAndStatus(
                        productId,
                        artistId,
                        ProductStatus.ACTIVE
                )
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "상품을 찾을 수 없습니다."
                ));
    }

    private Product updateStock(
            Product product,
            UpdateArtistProductStockRequest.Item item
    ) {
        product.updateStockQuantity(item.getStockQuantity(), item.getVersion());
        return product;
    }

    private Artist getArtistByUserId(Long userId) {
        return artistRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "작가 정보를 찾을 수 없습니다."
                ));
    }

    private int toPageIndex(int page) {
        return Math.max(page - FIRST_PAGE, FIRST_PAGE_INDEX);
    }

    private void validatePageSize(int size) {
        if (size < MIN_PAGE_SIZE) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "페이지 크기는 1 이상이어야 합니다.");
        }
    }

    private void validateDuplicateProductIds(UpdateArtistProductStockRequest request) {
        List<Long> productIds = request.getItems().stream()
                .map(UpdateArtistProductStockRequest.Item::getProductId)
                .toList();
        if (new HashSet<>(productIds).size() != productIds.size()) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "중복된 상품 ID가 포함되어 있습니다.");
        }
    }

    private boolean hasImageFile(MultipartFile productImage) {
        return productImage != null && !productImage.isEmpty();
    }
}
