package app.dearobjet.backend.domain.shop.service;

import app.dearobjet.backend.domain.shop.dto.DayBusinessHoursRequest;
import app.dearobjet.backend.domain.shop.dto.DayBusinessHoursResponse;
import app.dearobjet.backend.domain.shop.dto.ShopBusinessHoursResponse;
import app.dearobjet.backend.domain.shop.dto.UpdateBusinessHoursRequest;
import app.dearobjet.backend.domain.shop.entity.Shop;
import app.dearobjet.backend.domain.shop.entity.ShopBusinessHour;
import app.dearobjet.backend.domain.shop.repository.ShopBusinessHourRepository;
import app.dearobjet.backend.domain.user.repository.ShopRepository;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.ErrorCode;
import app.dearobjet.backend.global.exception.InvalidInputException;
import java.time.DayOfWeek;
import java.util.EnumMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final ShopBusinessHourRepository shopBusinessHourRepository;

    @Override
    public ShopBusinessHoursResponse updateBusinessHours(Long userId, UpdateBusinessHoursRequest request) {
        Shop shop = shopRepository.findByUser_Id(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND));

        Map<DayOfWeek, ShopBusinessHour> existingHours = new EnumMap<>(DayOfWeek.class);
        shopBusinessHourRepository.findAllByShop(shop)
                .forEach(hour -> existingHours.put(hour.getDayOfWeek(), hour));

        upsertDayHours(shop, existingHours, DayOfWeek.MONDAY, "monday", request.monday());
        upsertDayHours(shop, existingHours, DayOfWeek.TUESDAY, "tuesday", request.tuesday());
        upsertDayHours(shop, existingHours, DayOfWeek.WEDNESDAY, "wednesday", request.wednesday());
        upsertDayHours(shop, existingHours, DayOfWeek.THURSDAY, "thursday", request.thursday());
        upsertDayHours(shop, existingHours, DayOfWeek.FRIDAY, "friday", request.friday());
        upsertDayHours(shop, existingHours, DayOfWeek.SATURDAY, "saturday", request.saturday());
        upsertDayHours(shop, existingHours, DayOfWeek.SUNDAY, "sunday", request.sunday());

        return toBusinessHoursResponse(existingHours);
    }

    @Override
    @Transactional(readOnly = true)
    public ShopBusinessHoursResponse getBusinessHours(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND));

        Map<DayOfWeek, ShopBusinessHour> hoursByDay = new EnumMap<>(DayOfWeek.class);
        shopBusinessHourRepository.findAllByShop(shop)
                .forEach(hour -> hoursByDay.put(hour.getDayOfWeek(), hour));

        if (hoursByDay.isEmpty()) {
            return null;
        }

        return toBusinessHoursResponse(hoursByDay);
    }

    private void upsertDayHours(
            Shop shop,
            Map<DayOfWeek, ShopBusinessHour> existingHours,
            DayOfWeek dayOfWeek,
            String dayLabel,
            DayBusinessHoursRequest dayHours
    ) {
        Integer openMinutes = parseAndValidateTime(dayLabel, dayHours.openTime(), dayHours.closeTime(), true);
        Integer closeMinutes = parseAndValidateTime(dayLabel, dayHours.openTime(), dayHours.closeTime(), false);

        ShopBusinessHour businessHour = existingHours.get(dayOfWeek);
        if (businessHour == null) {
            businessHour = shopBusinessHourRepository.save(
                    ShopBusinessHour.builder()
                            .shop(shop)
                            .dayOfWeek(dayOfWeek)
                            .openMinutes(openMinutes)
                            .closeMinutes(closeMinutes)
                            .build()
            );
            existingHours.put(dayOfWeek, businessHour);
            return;
        }

        businessHour.update(openMinutes, closeMinutes);
    }

    private Integer parseAndValidateTime(String day, String openTime, String closeTime, boolean isOpenTime) {
        if (openTime == null && closeTime == null) {
            return null;
        }

        if (openTime == null || closeTime == null) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, day + " 운영시간은 시작/종료 시간을 함께 입력해야 합니다.");
        }

        int openMinutes = toMinutes(openTime);
        int closeMinutes = toMinutes(closeTime);
        if (openMinutes >= closeMinutes) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, day + " 운영시간은 시작 시간이 종료 시간보다 빨라야 합니다.");
        }

        return isOpenTime ? openMinutes : closeMinutes;
    }

    private ShopBusinessHoursResponse toBusinessHoursResponse(Map<DayOfWeek, ShopBusinessHour> hoursByDay) {
        return new ShopBusinessHoursResponse(
                toDayResponse(hoursByDay.get(DayOfWeek.MONDAY)),
                toDayResponse(hoursByDay.get(DayOfWeek.TUESDAY)),
                toDayResponse(hoursByDay.get(DayOfWeek.WEDNESDAY)),
                toDayResponse(hoursByDay.get(DayOfWeek.THURSDAY)),
                toDayResponse(hoursByDay.get(DayOfWeek.FRIDAY)),
                toDayResponse(hoursByDay.get(DayOfWeek.SATURDAY)),
                toDayResponse(hoursByDay.get(DayOfWeek.SUNDAY))
        );
    }

    private DayBusinessHoursResponse toDayResponse(ShopBusinessHour businessHour) {
        if (businessHour == null) {
            return new DayBusinessHoursResponse(null, null);
        }

        return new DayBusinessHoursResponse(
                formatMinutes(businessHour.getOpenMinutes()),
                formatMinutes(businessHour.getCloseMinutes())
        );
    }

    private int toMinutes(String time) {
        if ("24:00".equals(time)) {
            return 24 * 60;
        }

        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    private String formatMinutes(Integer minutes) {
        if (minutes == null) {
            return null;
        }

        if (minutes == 24 * 60) {
            return "24:00";
        }

        int hour = minutes / 60;
        int minute = minutes % 60;
        return String.format("%02d:%02d", hour, minute);
    }
}
