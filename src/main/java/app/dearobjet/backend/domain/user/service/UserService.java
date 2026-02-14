package app.dearobjet.backend.domain.user.service;

import app.dearobjet.backend.domain.user.dto.ArtistSignupRequest;
import app.dearobjet.backend.domain.user.dto.CustomerSignupRequest;
import app.dearobjet.backend.domain.user.dto.ShopSignupRequest;
import app.dearobjet.backend.domain.user.entity.User;

public interface UserService {

    // 카카오 OAuth 사용자 조회 또는 신규 생성
    User getOrCreateKakaoUser(String socialId);

    // CUSTOMER 가입
    void completeCustomerSignup(
            Long userId,
            CustomerSignupRequest request
    );

    // ARTIST 가입
    void completeArtistSignup(
            Long userId,
            ArtistSignupRequest request
    );

    void completeShopSignup(
            Long userId,
            ShopSignupRequest request
    );

    // 회원 비활성화
    void deactivateUser(Long userId);

    void changePhone(Long userId, String newPhone);
}

