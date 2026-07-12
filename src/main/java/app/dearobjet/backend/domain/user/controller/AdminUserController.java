package app.dearobjet.backend.domain.user.controller;

import app.dearobjet.backend.domain.user.dto.AdminRoleCountResponse;
import app.dearobjet.backend.domain.user.dto.AdminUserListResponse;
import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.service.AdminUserService;
import app.dearobjet.backend.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping("/role-counts")
    public ApiResponse<AdminRoleCountResponse> getRoleCounts() {
        return ApiResponse.of(adminUserService.getRoleCounts());
    }

    @GetMapping
    public ApiResponse<AdminUserListResponse> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.of(adminUserService.getUsers(page, role, keyword));
    }
}
