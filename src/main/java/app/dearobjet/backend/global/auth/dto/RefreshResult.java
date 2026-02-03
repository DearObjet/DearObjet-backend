package app.dearobjet.backend.global.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefreshResult {
    private String accessToken;
    private String refreshToken;
}

