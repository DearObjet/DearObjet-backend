package app.dearobjet.backend.domain.user.service;

import app.dearobjet.backend.domain.artist.entity.Artist;
import app.dearobjet.backend.domain.shop.client.KakaoLocalClient;
import app.dearobjet.backend.domain.shop.entity.Shop;
import app.dearobjet.backend.domain.shop.service.ShopCoordinate;
import app.dearobjet.backend.domain.user.dto.BusinessSignupRequest;
import app.dearobjet.backend.domain.user.dto.UserInfoResponse;
import app.dearobjet.backend.domain.user.dto.UserSignupRequest;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.enums.UserStatus;
import app.dearobjet.backend.domain.user.repository.ArtistRepository;
import app.dearobjet.backend.domain.user.repository.ShopRepository;
import app.dearobjet.backend.domain.user.repository.UserRepository;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.ErrorCode;
import app.dearobjet.backend.global.s3.service.S3FileUploadService;
import app.dearobjet.backend.global.sms.service.SmsAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;
    private final ShopRepository shopRepository;
    private final KakaoLocalClient kakaoLocalClient;
    private final S3FileUploadService s3FileUploadService;

    private final SmsAuthService smsAuthService;

    /**
     * 카카오 OAuth 로그인 사용자 조회 or 생성
     */
    @Override
    public User getOrCreateKakaoUser(String socialId, String email) {
        return userRepository.findBySocialId(socialId)
                .orElseGet(() -> createTempUser(socialId, email));
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(Long userId) {
        return UserInfoResponse.from(getUser(userId));
    }

    private User createTempUser(String socialId, String email) {
        User user = User.builder()
                .socialId(socialId)
                .email(email)
                .role(Role.TEMP)
                .userStatus(UserStatus.ACTIVE)
                .build();

        return userRepository.save(user);
    }


    // 일반 유저 회원가입
    public void completeSignup(Long userId, UserSignupRequest request) {

        User user = getUser(userId);

        user.completeRegistration(
                request.getName(),
                request.getPhoneNumber(),
                request.getSmsAgreement(),
                request.getMarketingAgreement()
        );

        user.changeRole(Role.CUSTOMER);
    }

    public void completeArtistSignup(
            Long userId,
            BusinessSignupRequest request,
            MultipartFile businessLicenseFile
    ) {

        User user = getUser(userId);
        String businessLicenseUrl = s3FileUploadService.uploadBusinessLicense(businessLicenseFile, userId);

        user.completeRegistration(
                request.getOwnerName(),
                request.getPhoneNumber(),
                request.getSmsAgreement(),
                request.getMarketingAgreement()
        );

        user.changeRole(Role.ARTIST);

        Artist artist = Artist.builder()
                .user(user)
                .businessNumber(request.getBusinessNumber())
                .businessName(request.getBusinessName())
                .ownerName(request.getOwnerName())
                .businessAddress(request.getBusinessAddress())
                .businessLicenseUrl(businessLicenseUrl)
                .businessType(request.getBusinessType())
                .businessCategory(request.getBusinessCategory())
                .specialty(request.getSpecialty())
                .reviewDataAgreement(Boolean.TRUE.equals(request.getReviewDataAgreement()))
                .build();

        artistRepository.save(artist);
    }

    public void completeShopSignup(
            Long userId,
            BusinessSignupRequest request,
            MultipartFile businessLicenseFile
    ) {

        User user = getUser(userId);
        String businessLicenseUrl = s3FileUploadService.uploadBusinessLicense(businessLicenseFile, userId);

        user.completeRegistration(
                request.getOwnerName(),
                request.getPhoneNumber(),
                request.getSmsAgreement(),
                request.getMarketingAgreement()
        );

        user.changeRole(Role.SHOP);

        ShopCoordinate coordinate = kakaoLocalClient.searchAddress(request.getBusinessAddress());

        Shop shop = Shop.builder()
                .user(user)
                .businessNumber(request.getBusinessNumber())
                .businessName(request.getBusinessName())
                .ownerName(request.getOwnerName())
                .businessAddress(request.getBusinessAddress())
                .latitude(coordinate == null ? null : coordinate.latitude())
                .longitude(coordinate == null ? null : coordinate.longitude())
                .businessLicenseUrl(businessLicenseUrl)
                .businessType(request.getBusinessType())
                .businessCategory(request.getBusinessCategory())
                .specialty(request.getSpecialty())
                .reviewDataAgreement(Boolean.TRUE.equals(request.getReviewDataAgreement()))
                .build();

        shopRepository.save(shop);
    }

    /**
     * 회원 비활성화
     */
    @Override
    public void deactivateUser(Long userId) {
        User user = getUser(userId);

        user.deactivate();
    }

    @Override
    public void changePhone(Long userId, String newPhone) {

        // 1. 서버 기준 인증 확인
        smsAuthService.assertVerified(newPhone);

        User user = getUser(userId);

        user.changePhone(newPhone);

        // 2. 인증 1회성 소모
        smsAuthService.consumeVerified(newPhone);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
    }
}
