package app.dearobjet.backend.domain.classes;

import app.dearobjet.backend.domain.classes.dto.AvailableClassSlotsResponse;
import app.dearobjet.backend.domain.classes.dto.ClassReservationListResponse;
import app.dearobjet.backend.domain.shop.entity.ShopBusinessHour;
import app.dearobjet.backend.domain.shop.repository.ShopBusinessHourRepository;
import app.dearobjet.backend.domain.user.repository.ShopRepository;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.ErrorCode;
import app.dearobjet.backend.global.exception.InvalidInputException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClassReservationService {
    private static final DateTimeFormatter TIME_LABEL_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final ClassReservationRepository classReservationRepository;
    private final ShopRepository shopRepository;
    private final ClassesRepository classesRepository;
    private final ClassSessionRepository classSessionRepository;
    private final ShopBusinessHourRepository shopBusinessHourRepository;

    @Transactional(readOnly = true)
    public ClassReservationListResponse getReservations(Long userId, String status, int page, int size) {
        shopRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found for user: " + userId));

        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                size,
                Sort.by(Sort.Direction.DESC, "reservationId")
        );

        Page<ClassReservation> reservationPage = (status == null || status.isBlank())
                ? classReservationRepository.findByClasses_Shop_User_Id(userId, pageable)
                : classReservationRepository.findByClasses_Shop_User_IdAndReservationStatus(
                userId,
                status,
                pageable
        );

        List<ClassReservationListResponse.Item> items = new ArrayList<>();
        for (ClassReservation reservation : reservationPage.getContent()) {
            items.add(new ClassReservationListResponse.Item(
                    reservation.getReservationStatus(),
                    reservation.getUser() == null ? null : reservation.getUser().getName(),
                    reservation.getUser() == null ? null : reservation.getUser().getPhoneNumber(),
                    reservation.getReservationId(),
                    reservation.getReservationTime(),
                    reservation.getClasses() == null ? null : reservation.getClasses().getClassName()
            ));
        }

        return new ClassReservationListResponse(
                reservationPage.getTotalElements(),
                items,
                page,
                reservationPage.getTotalPages()
        );
    }

    @Transactional
    public AvailableClassSlotsResponse getAvailableSlots(Long classId, LocalDate date) {
        if (date == null) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "예약 날짜는 필수입니다.");
        }

        Classes classes = classesRepository.findById(classId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "클래스를 찾을 수 없습니다."));

        ShopBusinessHour businessHour = shopBusinessHourRepository.findByShopAndDayOfWeek(
                        classes.getShop(),
                        date.getDayOfWeek()
                )
                .orElse(null);

        if (businessHour == null || businessHour.getOpenMinutes() == null || businessHour.getCloseMinutes() == null) {
            return new AvailableClassSlotsResponse(classId, date, null, null, List.of());
        }

        int openMinutes = businessHour.getOpenMinutes();
        int closeMinutes = businessHour.getCloseMinutes();
        if (openMinutes >= closeMinutes) {
            return new AvailableClassSlotsResponse(classId, date, null, null, List.of());
        }

        List<ClassSession> sessions = syncSessions(classes, date, openMinutes, closeMinutes);

        List<ClassReservation> reservations = classReservationRepository.findByClasses_ClassesIdAndReservationTimeBetween(
                classId,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay().minusNanos(1)
        );

        Map<LocalDateTime, Integer> reservedGuestCountByTime = new HashMap<>();
        for (ClassReservation reservation : reservations) {
            reservedGuestCountByTime.merge(
                    reservation.getReservationTime(),
                    reservation.getGuestCount() == null ? 1 : reservation.getGuestCount(),
                    Integer::sum
            );
        }

        Integer maxCapacity = classes.getMaxCapacity();
        LocalDateTime now = LocalDateTime.now();
        List<AvailableClassSlotsResponse.Slot> slots = new ArrayList<>();

        for (ClassSession session : sessions) {
            LocalDateTime slotTime = session.getStartDatetime();
            if (slotTime.isBefore(now)) {
                continue;
            }

            int reservedGuestCount = reservedGuestCountByTime.getOrDefault(slotTime, 0);
            Integer sessionCapacity = session.getCapacity() != null ? session.getCapacity() : maxCapacity;
            Integer remainingCapacity = sessionCapacity == null ? null : Math.max(sessionCapacity - reservedGuestCount, 0);

            if (remainingCapacity != null && remainingCapacity <= 0) {
                continue;
            }

            slots.add(new AvailableClassSlotsResponse.Slot(
                    session.getSessionId(),
                    slotTime,
                    slotTime.toLocalTime().format(TIME_LABEL_FORMATTER),
                    remainingCapacity
            ));
        }

        return new AvailableClassSlotsResponse(
                classId,
                date,
                formatMinutes(openMinutes),
                formatMinutes(closeMinutes),
                slots
        );
    }

    private List<ClassSession> syncSessions(Classes classes, LocalDate date, int openMinutes, int closeMinutes) {
        List<ClassSession> existingSessions = classSessionRepository
                .findByClasses_ClassesIdAndStartDatetimeBetweenOrderByStartDatetimeAsc(
                        classes.getClassesId(),
                        date.atStartOfDay(),
                        date.plusDays(1).atStartOfDay().minusNanos(1)
                );

        Map<LocalDateTime, ClassSession> existingByStartTime = new HashMap<>();
        for (ClassSession existingSession : existingSessions) {
            existingByStartTime.put(existingSession.getStartDatetime(), existingSession);
        }

        List<ClassSession> newSessions = new ArrayList<>();
        for (int minutes = openMinutes; minutes + 60 <= closeMinutes; minutes += 60) {
            LocalDateTime startDatetime = date.atTime(LocalTime.of(minutes / 60, minutes % 60));
            LocalDateTime endDatetime = startDatetime.plusHours(1);

            ClassSession existingSession = existingByStartTime.get(startDatetime);
            if (existingSession != null) {
                if (classes.getMaxCapacity() != null) {
                    existingSession.updateCapacity(classes.getMaxCapacity());
                }
                if (existingSession.getSessionStatus() == null || existingSession.getSessionStatus().isBlank()) {
                    existingSession.updateSessionStatus("OPEN");
                }
                continue;
            }

            newSessions.add(ClassSession.builder()
                    .classes(classes)
                    .shop(classes.getShop())
                    .startDatetime(startDatetime)
                    .endDatetime(endDatetime)
                    .capacity(classes.getMaxCapacity())
                    .sessionStatus("OPEN")
                    .build());
        }

        if (!newSessions.isEmpty()) {
            existingSessions = new ArrayList<>(existingSessions);
            existingSessions.addAll(classSessionRepository.saveAll(newSessions));
        }

        existingSessions.sort(Comparator.comparing(ClassSession::getStartDatetime));
        return existingSessions;
    }

    private String formatMinutes(int minutes) {
        if (minutes == 24 * 60) {
            return "24:00";
        }

        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }
}
