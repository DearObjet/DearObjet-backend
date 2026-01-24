package app.dearobjet.backend.global.auth.security;

import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.enums.UserStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public Long getUserId() {
        return user.getUserId();
    }

    public Role getRole() {
        return user.getRole();
    }

    public UserStatus getUserStatus() {
        return user.getUserStatus();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }

    /**
     * OAuth/JWT 구조라 비밀번호 사용 안 함
     */
    @Override
    public String getPassword() {
        return null;
    }

    /**
     * username = 식별자
     * → JWT subject / OAuth 이후 인증 기준
     */
    @Override
    public String getUsername() {
        return String.valueOf(user.getUserId());
    }

    /**
     * 상태 기반 계정 제어
     */
    @Override
    public boolean isEnabled() {
        return user.getUserStatus() == UserStatus.ACTIVE;
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
}
