package app.dearobjet.backend.domain.user.service;

import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.enums.UserStatus;
import app.dearobjet.backend.domain.user.repository.UserRepository;
import app.dearobjet.backend.global.sms.service.SmsAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
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

    /**
     * 추가 회원가입 완료
     */
    @Override
    public void completeSignup(
            Long userId,
            String name,
            String email,
            String phoneNumber,
            Boolean smsAgreement,
            Boolean marketingAgreement,
            Role role
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 이메일 중복 체크
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Email already exists");
        }

        user.completeRegistration(
                name,
                email,
                phoneNumber,
                smsAgreement,
                marketingAgreement,
                role
        );
    }

    /**
     * 회원 비활성화
     */
    @Override
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.deactivate();
    }

    @Override
    public void changePhone(Long userId, String newPhone) {

        // 1. 서버 기준 인증 확인
        smsAuthService.assertVerified(newPhone);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.changePhone(newPhone);

        // 2. 인증 1회성 소모
        smsAuthService.consumeVerified(newPhone);
    }
}
