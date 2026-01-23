package app.dearobjet.backend.domain.user.service;

import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.enums.Role;

public interface UserService {

    // 카카오 OAuth 사용자 조회 또는 신규 생성
    User getOrCreateKakaoUser(String socialId);

    // 추가 회원가입
    void completeSignup(
            Long userId,
            String name,
            String email,
            String phoneNumber,
            Boolean smsAgreement,
            Boolean marketingAgreement,
            Role role
    );

    // 회원 비활성화
    void deactivateUser(Long userId);
}

