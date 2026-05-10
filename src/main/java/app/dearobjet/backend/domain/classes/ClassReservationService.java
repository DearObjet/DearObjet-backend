package app.dearobjet.backend.domain.classes;

import app.dearobjet.backend.domain.classes.dto.AvailableClassSlotsResponse;
import app.dearobjet.backend.domain.classes.dto.ClassReservationListResponse;
import app.dearobjet.backend.domain.classes.dto.CreateClassReservationRequest;
import app.dearobjet.backend.domain.classes.dto.CreateClassReservationResponse;
import app.dearobjet.backend.domain.classes.dto.MyClassReservationsResponse;
import app.dearobjet.backend.domain.shop.entity.ShopBusinessHour;
import app.dearobjet.backend.domain.shop.repository.ShopBusinessHourRepository;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.repository.ShopRepository;
import app.dearobjet.backend.domain.user.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ClassReservationListResponse getReservations(Long userId, String status, int page, int size) {
        shopRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found for user: " + userId));

        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                size,
                Sort.by(Sort.Direction.DESC, "reservationId")
        );

        ClassReservationStatus reservationStatus = parseReservationStatus(status);
        Page<ClassReservation> reservationPage = (status == null || status.isBlank())
                ? classReservationRepository.findByClasses_Shop_User_Id(userId, pageable)
                : classReservationRepository.findByClasses_Shop_User_IdAndReservationStatus(
                userId,
                reservationStatus,
                pageable
        );

        List<ClassReservationListResponse.Item> items = new ArrayList<>();
        for (ClassReservation reservation : reservationPage.getContent()) {
            items.add(new ClassReservationListResponse.Item(
                    reservation.getReservationStatus() == null ? null : reservation.getReservationStatus().name(),
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

    @Transactional(readOnly = true)
    public MyClassReservationsResponse getMyReservations(Long userId) {
        List<ClassReservation> reservations = classReservationRepository.findByUser_IdOrderByReservationTimeDesc(userId);
        if (reservations.isEmpty()) {
            throw new EntityNotFoundException(ErrorCode.CLASS_RESERVATION_NOT_FOUND);
        }

        LocalDateTime now = LocalDateTime.now();
        List<MyClassReservationsResponse.ReservationSummary> currentReservations = new ArrayList<>();
        List<MyClassReservationsResponse.ReservationSummary> pastReservations = new ArrayList<>();

        for (ClassReservation reservation : reservations) {
            MyClassReservationsResponse.ReservationSummary item = new MyClassReservationsResponse.ReservationSummary(
                    reservation.getReservationId(),
                    reservation.getReservationStatus() == null ? null : reservation.getReservationStatus().name(),
                    reservation.getClasses() == null || reservation.getClasses().getShop() == null
                            ? null
                            : reservation.getClasses().getShop().getShopName(),
                    reservation.getReservationTime(),
                    reservation.getClasses() == null ? null : reservation.getClasses().getClassName()
            );

            if (isCurrentReservation(reservation, now)) {
                currentReservations.add(item);
                continue;
            }

            pastReservations.add(item);
        }

        return new MyClassReservationsResponse(currentReservations, pastReservations);
    }

    @Transactional
    public void cancelReservation(Long userId, Long reservationId) {
        ClassReservation reservation = classReservationRepository.findByReservationIdAndUser_Id(reservationId, userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "예약 내역을 찾을 수 없습니다."));

        validateCancelableReservation(reservation);
        reservation.cancel();
    }

    @Transactional
    public CreateClassReservationResponse createReservation(Long userId, CreateClassReservationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        ClassReservation reservation = createReservationInternal(user, request, null);

        return new CreateClassReservationResponse(
                reservation.getReservationId(),
                reservation.getReservationStatus().name()
        );
    }

    @Transactional
    public CreateClassReservationResponse changeReservation(
            Long userId,
            Long reservationId,
            CreateClassReservationRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
        ClassReservation originalReservation = classReservationRepository.findByReservationIdAndUser_Id(reservationId, userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "예약 내역을 찾을 수 없습니다."));

        validateCancelableReservation(originalReservation);

        ClassReservation changedReservation = createReservationInternal(user, request, originalReservation);
        originalReservation.cancel();

        return new CreateClassReservationResponse(
                changedReservation.getReservationId(),
                changedReservation.getReservationStatus().name()
        );
    }

    private ClassReservation createReservationInternal(
            User user,
            CreateClassReservationRequest request,
            ClassReservation reservationToReplace
    ) {
        Long userId = user.getId();

        // 프론트가 슬롯 조회에서 받은 sessionId를 그대로 예약에 사용한다.
        ClassSession session = classSessionRepository.findBySessionId(request.sessionId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "예약 가능한 슬롯을 찾을 수 없습니다."));
        Classes classes = session.getClasses();
        if (hasBlockingPendingReservation(userId, reservationToReplace)) {
            throw new InvalidInputException(ErrorCode.ACTIVE_CLASS_RESERVATION_ALREADY_EXISTS);
        }

        validateReservationRequest(user, request, classes, session);

        int reservedGuestCount = classReservationRepository.sumGuestCountBySessionId(session.getSessionId());
        if (isReplacingSameSessionReservation(reservationToReplace, session)) {
            reservedGuestCount -= reservationToReplace.getGuestCount() == null ? 1 : reservationToReplace.getGuestCount();
        }
        int capacity = session.getCapacity() != null
                ? session.getCapacity()
                : (classes.getMaxCapacity() == null ? 0 : classes.getMaxCapacity());

        if (capacity <= 0) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "예약 가능한 정원이 없습니다.");
        }

        if (reservedGuestCount + request.guestCount() > capacity) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "남은 예약 가능 인원을 초과했습니다.");
        }

        // 신청 단계에서는 바로 확정하지 않고 PENDING 으로 저장한다.
        return classReservationRepository.save(ClassReservation.builder()
                .guestCount(request.guestCount())
                .reservationTime(session.getStartDatetime())
                .reservationStatus(ClassReservationStatus.PENDING)
                .reservationName(request.reservationName())
                .memo(request.memo())
                .user(user)
                .classes(classes)
                .classSession(session)
                .build());
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
            return new AvailableClassSlotsResponse(date, null, null, List.of());
        }

        int openMinutes = businessHour.getOpenMinutes();
        int closeMinutes = businessHour.getCloseMinutes();
        if (openMinutes >= closeMinutes) {
            return new AvailableClassSlotsResponse(date, null, null, List.of());
        }

        // 슬롯은 영업시간 기준으로 class_sessions 에 동기화한 뒤 응답에 사용한다.
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
            // 프론트가 "예약 가능 / 마감"을 바로 표시할 수 있도록 슬롯은 숨기지 않고 상태만 내려준다.
            boolean available = remainingCapacity == null || remainingCapacity > 0;

            slots.add(new AvailableClassSlotsResponse.Slot(
                    session.getSessionId(),
                    slotTime.toLocalTime().format(TIME_LABEL_FORMATTER),
                    remainingCapacity,
                    available
            ));
        }

        return new AvailableClassSlotsResponse(
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
                // 이미 있던 슬롯은 정원/상태만 최신 클래스 설정에 맞춰 보정한다.
                if (classes.getMaxCapacity() != null) {
                    existingSession.updateCapacity(classes.getMaxCapacity());
                }
                if (existingSession.getSessionStatus() == null || existingSession.getSessionStatus().isBlank()) {
                    existingSession.updateSessionStatus("OPEN");
                }
                continue;
            }

            // 아직 없는 슬롯만 영업시간 기준으로 1시간 단위 생성한다.
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

        List<ClassSession> sortedSessions = new ArrayList<>(existingSessions);
        sortedSessions.sort(Comparator.comparing(ClassSession::getStartDatetime));
        return sortedSessions;
    }

    private void validateReservationRequest(
            User user,
            CreateClassReservationRequest request,
            Classes classes,
            ClassSession session
    ) {
        if (classes.getShop() != null
                && classes.getShop().getUser() != null
                && classes.getShop().getUser().getId() != null
                && classes.getShop().getUser().getId().equals(user.getId())) {
            throw new InvalidInputException(
                    ErrorCode.CLASS_OWNER_CANNOT_RESERVE,
                    "클래스를 개설한 본인은 예약할 수 없습니다."
            );
        }

        if (!session.getClasses().getClassesId().equals(classes.getClassesId())) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "클래스와 예약 슬롯이 일치하지 않습니다.");
        }

        if (session.getStartDatetime().isBefore(LocalDateTime.now())) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "지난 시간은 예약할 수 없습니다.");
        }

        if (session.getSessionStatus() != null && !"OPEN".equalsIgnoreCase(session.getSessionStatus())) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "현재 예약할 수 없는 슬롯입니다.");
        }
    }

    private void validateCancelableReservation(ClassReservation reservation) {
        if (reservation.getReservationStatus() == ClassReservationStatus.CANCELED) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "이미 취소된 예약입니다.");
        }

        if (reservation.getReservationTime() != null && reservation.getReservationTime().isBefore(LocalDateTime.now())) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "지난 예약은 취소할 수 없습니다.");
        }
    }

    private boolean hasBlockingPendingReservation(Long userId, ClassReservation reservationToReplace) {
        if (reservationToReplace == null) {
            return classReservationRepository.existsByUser_IdAndReservationStatus(
                    userId,
                    ClassReservationStatus.PENDING
            );
        }

        return classReservationRepository.existsByUser_IdAndReservationStatusAndReservationIdNot(
                userId,
                ClassReservationStatus.PENDING,
                reservationToReplace.getReservationId()
        );
    }

    private boolean isReplacingSameSessionReservation(
            ClassReservation reservationToReplace,
            ClassSession newSession
    ) {
        return reservationToReplace != null
                && reservationToReplace.getClassSession() != null
                && reservationToReplace.getClassSession().getSessionId() != null
                && reservationToReplace.getClassSession().getSessionId().equals(newSession.getSessionId())
                && reservationToReplace.getReservationStatus() != ClassReservationStatus.CANCELED;
    }

    private ClassReservationStatus parseReservationStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return ClassReservationStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "예약 상태는 PENDING, CONFIRMED, CANCELED 중 하나여야 합니다.");
        }
    }

    private boolean isCurrentReservation(ClassReservation reservation, LocalDateTime now) {
        return reservation.getReservationStatus() != ClassReservationStatus.CANCELED
                && reservation.getReservationTime() != null
                && !reservation.getReservationTime().isBefore(now);
    }

    private String formatMinutes(int minutes) {
        if (minutes == 24 * 60) {
            return "24:00";
        }

        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }
}
