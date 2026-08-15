package app.dearobjet.backend.domain.user.service;

import app.dearobjet.backend.domain.user.dto.AdminRoleCountResponse;
import app.dearobjet.backend.domain.user.dto.AdminUserDetailResponse;
import app.dearobjet.backend.domain.user.dto.AdminUserListResponse;
import app.dearobjet.backend.domain.user.dto.AdminUserStatusResponse;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.enums.UserStatus;
import app.dearobjet.backend.domain.user.repository.UserRepository;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.ErrorCode;
import app.dearobjet.backend.global.exception.InvalidInputException;
import app.dearobjet.backend.global.exception.UnauthorizedException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final int PAGE_SIZE = 20;

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public AdminRoleCountResponse getRoleCounts() {
        return new AdminRoleCountResponse(
                userRepository.countByRole(Role.CUSTOMER),
                userRepository.countByRole(Role.SHOP),
                userRepository.countByRole(Role.ARTIST)
        );
    }

    @Transactional(readOnly = true)
    public AdminUserListResponse getUsers(int page, Role role, String keyword) {
        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"))
        );

        String keywordPattern = StringUtils.hasText(keyword)
                ? "%" + keyword.strip().toLowerCase() + "%"
                : null;

        Page<User> userPage = userRepository.searchForAdmin(role, keywordPattern, pageable);

        List<AdminUserListResponse.UserSummary> users = userPage.getContent().stream()
                .map(user -> new AdminUserListResponse.UserSummary(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getCreatedAt(),
                        user.getUserStatus()
                ))
                .toList();

        return new AdminUserListResponse(users, page, userPage.getTotalPages(), userPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() == Role.ADMIN) {
            throw new EntityNotFoundException(ErrorCode.USER_NOT_FOUND);
        }

        return AdminUserDetailResponse.from(user);
    }

    @Transactional
    public AdminUserStatusResponse updateStatus(Long userId, UserStatus status) {
        if (status != UserStatus.ACTIVE && status != UserStatus.INACTIVE) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "활성 또는 비활성으로만 변경할 수 있습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() == Role.ADMIN) {
            throw new UnauthorizedException(ErrorCode.FORBIDDEN, "관리자 계정의 상태는 변경할 수 없습니다.");
        }

        if (user.isWithdrawn()) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "탈퇴한 회원의 상태는 변경할 수 없습니다.");
        }

        if (user.isWithdrawalRequested()) {
            if (status != UserStatus.ACTIVE) {
                throw new InvalidInputException(ErrorCode.INVALID_INPUT, "탈퇴 진행 중인 회원은 탈퇴 취소만 가능합니다.");
            }
            user.recoverWithdrawal();
        } else if (status == UserStatus.ACTIVE) {
            user.activate();
        } else {
            user.deactivate();
        }

        return new AdminUserStatusResponse(user.getId(), user.getUserStatus());
    }
}
