package app.dearobjet.backend.domain.artist.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public enum ArtistShipmentInboundStatus {

    CONFIRMED("확인"),
    UNCONFIRMED("미확인"),
    HOLD("보류");

    private final String label;

    public static ArtistShipmentInboundStatus from(LocalDateTime recentStockedAt, Boolean inboundConfirmed) {
        if (recentStockedAt == null) {
            return HOLD;
        }
        if (Boolean.TRUE.equals(inboundConfirmed)) {
            return CONFIRMED;
        }
        return UNCONFIRMED;
    }
}
