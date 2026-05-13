package app.dearobjet.backend.domain.shop.service;

import app.dearobjet.backend.domain.shop.dto.CreateShopReviewRequest;
import app.dearobjet.backend.domain.shop.dto.ShopReviewListResponse;
import app.dearobjet.backend.domain.shop.dto.ShopReviewResponse;
import app.dearobjet.backend.domain.shop.dto.UpdateShopReviewRequest;
import app.dearobjet.backend.domain.shop.entity.Shop;
import app.dearobjet.backend.domain.shop.entity.ShopReview;
import app.dearobjet.backend.domain.shop.repository.ShopReviewRepository;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.repository.ShopRepository;
import app.dearobjet.backend.domain.user.repository.UserRepository;
import app.dearobjet.backend.global.exception.BusinessException;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.ErrorCode;
import app.dearobjet.backend.global.exception.InvalidInputException;
import app.dearobjet.backend.global.s3.service.S3FileUploadService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class ShopReviewService {

    private static final int MAX_REVIEW_PAGE_SIZE = 50;

    private final ShopReviewRepository shopReviewRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final S3FileUploadService s3FileUploadService;

    @Transactional(readOnly = true)
    public ShopReviewListResponse getReviews(Long currentUserId, Long shopId, Long cursorId, int size) {
        validatePageSize(size);
        ensureShopExists(shopId);

        PageRequest pageRequest = PageRequest.of(
                0,
                size + 1,
                Sort.by(Sort.Direction.DESC, "shopReviewId")
        );
        List<ShopReview> fetchedReviews = cursorId == null
                ? shopReviewRepository.findByShop_ShopId(shopId, pageRequest)
                : shopReviewRepository.findByShop_ShopIdAndShopReviewIdLessThan(shopId, cursorId, pageRequest);

        boolean hasNext = fetchedReviews.size() > size;
        List<ShopReview> reviews = hasNext ? fetchedReviews.subList(0, size) : fetchedReviews;
        Long nextCursorId = hasNext && !reviews.isEmpty()
                ? reviews.get(reviews.size() - 1).getShopReviewId()
                : null;

        List<ShopReviewResponse> items = reviews.stream()
                .map(review -> toResponse(review, currentUserId))
                .toList();
        return new ShopReviewListResponse(items, nextCursorId, hasNext);
    }

    public ShopReviewResponse createReview(
            Long userId,
            Long shopId,
            CreateShopReviewRequest request,
            MultipartFile image
    ) {
        User user = findUser(userId);
        Shop shop = findShop(shopId);
        String imageUrl = s3FileUploadService.uploadShopReviewImage(image, userId);

        ShopReview review = shopReviewRepository.save(ShopReview.builder()
                .shop(shop)
                .user(user)
                .title(request.title())
                .content(request.content())
                .imageUrl(imageUrl)
                .build());

        return toResponse(review, userId);
    }

    public ShopReviewResponse updateReview(
            Long userId,
            Long shopId,
            Long reviewId,
            UpdateShopReviewRequest request,
            MultipartFile image
    ) {
        ShopReview review = findReview(shopId, reviewId);
        validateOwner(review, userId);

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = s3FileUploadService.uploadShopReviewImage(image, userId);
        }

        review.update(request.title(), request.content(), imageUrl);
        return toResponse(review, userId);
    }

    public void deleteReview(Long userId, Long shopId, Long reviewId) {
        ShopReview review = findReview(shopId, reviewId);
        validateOwner(review, userId);
        shopReviewRepository.delete(review);
    }

    private void validatePageSize(int size) {
        if (size < 1 || size > MAX_REVIEW_PAGE_SIZE) {
            throw new InvalidInputException(
                    ErrorCode.INVALID_INPUT,
                    "리뷰 조회 크기는 1개 이상 " + MAX_REVIEW_PAGE_SIZE + "개 이하여야 합니다."
            );
        }
    }

    private void ensureShopExists(Long shopId) {
        if (!shopRepository.existsById(shopId)) {
            throw new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "상점을 찾을 수 없습니다.");
        }
    }

    private Shop findShop(Long shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "상점을 찾을 수 없습니다."));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    private ShopReview findReview(Long shopId, Long reviewId) {
        return shopReviewRepository.findByShopReviewIdAndShop_ShopId(reviewId, shopId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "리뷰를 찾을 수 없습니다."));
    }

    private void validateOwner(ShopReview review, Long userId) {
        if (!review.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private ShopReviewResponse toResponse(ShopReview review, Long currentUserId) {
        User user = review.getUser();
        return new ShopReviewResponse(
                review.getShopReviewId(),
                user.getId(),
                user.getName(),
                user.getProfileUrl(),
                review.getTitle(),
                review.getContent(),
                review.getImageUrl(),
                review.isOwnedBy(currentUserId),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
