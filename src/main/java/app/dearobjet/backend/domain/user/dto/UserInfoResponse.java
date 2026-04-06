package app.dearobjet.backend.domain.user.dto;

import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoResponse {
    private final String email;
    private final String name;
    private final String profileImage;
    private final Role role;

    public static UserInfoResponse from(User user) {
        return new UserInfoResponse(
                user.getEmail(),
                user.getName(),
                user.getProfileImage(),
                user.getRole()
        );
    }
}
