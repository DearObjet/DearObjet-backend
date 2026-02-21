package app.dearobjet.backend.domain.user.service;

import app.dearobjet.backend.domain.artist.entity.Artist;
import app.dearobjet.backend.domain.shop.entity.Shop;
import app.dearobjet.backend.domain.user.dto.BusinessSignupRequest;
import app.dearobjet.backend.domain.user.dto.UserSignupRequest;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.enums.UserStatus;
import app.dearobjet.backend.domain.user.repository.ArtistRepository;
import app.dearobjet.backend.domain.user.repository.ShopRepository;
import app.dearobjet.backend.domain.user.repository.UserRepository;
import app.dearobjet.backend.global.exception.DuplicateEntityException;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.ErrorCode;
import app.dearobjet.backend.global.sms.service.SmsAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;
    private final ShopRepository shopRepository;

    private final SmsAuthService smsAuthService;

    /**
     * 카카오 OAuth 로그인 사용자 조회 or 생성
     */
    @Override
    public User getOrCreateKakaoUser(String socialId) {
        return userRepository.findBySocialId(socialId)
                .orElseGet(() -> createTempUser(socialId));
    }

    private User createTempUser(String socialId) {
        User user = User.builder()
                .socialId(socialId)
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

        user.changeRole(Role.USER);
    }

    public void completeArtistSignup(Long userId, BusinessSignupRequest request) {

        User user = getUser(userId);

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
                .businessLicenseUrl(request.getBusinessLicenseUrl())
                .businessType(request.getBusinessType())
                .businessCategory(request.getBusinessCategory())
                .specialty(request.getSpecialty())
                .reviewDataAgreement(Boolean.TRUE.equals(request.getReviewDataAgreement()))
                .build();

        artistRepository.save(artist);
    }

    public void completeShopSignup(Long userId, BusinessSignupRequest request) {

        User user = getUser(userId);

        user.completeRegistration(
                request.getOwnerName(),
                request.getPhoneNumber(),
                request.getSmsAgreement(),
                request.getMarketingAgreement()
        );

        user.changeRole(Role.SHOP);

        Shop shop = Shop.builder()
                .user(user)
                .businessNumber(request.getBusinessNumber())
                .businessName(request.getBusinessName())
                .ownerName(request.getOwnerName())
                .businessAddress(request.getBusinessAddress())
                .businessLicenseUrl(request.getBusinessLicenseUrl())
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
