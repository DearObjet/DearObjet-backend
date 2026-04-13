package app.dearobjet.backend.domain.shop.dto;

public record ShopBusinessHoursResponse(
        DayBusinessHoursResponse monday,
        DayBusinessHoursResponse tuesday,
        DayBusinessHoursResponse wednesday,
        DayBusinessHoursResponse thursday,
        DayBusinessHoursResponse friday,
        DayBusinessHoursResponse saturday,
        DayBusinessHoursResponse sunday
) {
}
