package app.dearobjet.backend.domain.shop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record UpdateBusinessHoursRequest(
        @NotNull(message = "월요일 운영시간은 필수입니다.")
        @Valid
        DayBusinessHoursRequest monday,

        @NotNull(message = "화요일 운영시간은 필수입니다.")
        @Valid
        DayBusinessHoursRequest tuesday,

        @NotNull(message = "수요일 운영시간은 필수입니다.")
        @Valid
        DayBusinessHoursRequest wednesday,

        @NotNull(message = "목요일 운영시간은 필수입니다.")
        @Valid
        DayBusinessHoursRequest thursday,

        @NotNull(message = "금요일 운영시간은 필수입니다.")
        @Valid
        DayBusinessHoursRequest friday,

        @NotNull(message = "토요일 운영시간은 필수입니다.")
        @Valid
        DayBusinessHoursRequest saturday,

        @NotNull(message = "일요일 운영시간은 필수입니다.")
        @Valid
        DayBusinessHoursRequest sunday
) {
}
