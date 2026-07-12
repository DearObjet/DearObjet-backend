package app.dearobjet.backend.domain.user.service;

import app.dearobjet.backend.domain.user.dto.AdminRoleCountResponse;
import app.dearobjet.backend.domain.user.dto.AdminUserListResponse;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.repository.UserRepository;
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
}
