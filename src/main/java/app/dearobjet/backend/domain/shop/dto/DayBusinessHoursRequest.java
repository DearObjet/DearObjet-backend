package app.dearobjet.backend.domain.shop.dto;

import jakarta.validation.constraints.Pattern;

public record DayBusinessHoursRequest(
        @Pattern(
                regexp = "^(([01][0-9]|2[0-3]):[0-5][0-9]|24:00)$",
                message = "운영 시작 시간은 HH:mm 형식이어야 합니다."
        )
        String openTime,

        @Pattern(
                regexp = "^(([01][0-9]|2[0-3]):[0-5][0-9]|24:00)$",
                message = "운영 종료 시간은 HH:mm 형식이어야 합니다."
        )
        String closeTime
) {
}
