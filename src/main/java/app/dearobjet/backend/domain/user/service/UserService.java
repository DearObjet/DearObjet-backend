package app.dearobjet.backend.domain.user.service;

import app.dearobjet.backend.domain.user.dto.BusinessSignupRequest;
import app.dearobjet.backend.domain.user.dto.UserInfoResponse;
import app.dearobjet.backend.domain.user.dto.UserSignupRequest;
import app.dearobjet.backend.domain.user.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    // 카카오 OAuth 사용자 조회 또는 신규 생성
    User getOrCreateKakaoUser(String socialId, String email);

    UserInfoResponse getUserInfo(Long userId);

    // CUSTOMER 가입
    void completeSignup(
            Long userId,
            UserSignupRequest request
    );

    void completeArtistSignup(
            Long userId,
            BusinessSignupRequest request,
            MultipartFile businessLicenseFile
    );

    void completeShopSignup(
            Long userId,
            BusinessSignupRequest request,
            MultipartFile businessLicenseFile
    );

    // 회원 비활성화
    void deactivateUser(Long userId);

    void changePhone(Long userId, String newPhone);
}

