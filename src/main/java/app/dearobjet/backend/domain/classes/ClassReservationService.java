package app.dearobjet.backend.domain.classes;

import app.dearobjet.backend.domain.classes.dto.ClassReservationListResponse;
import app.dearobjet.backend.domain.classes.dto.ClassReservationResponse;
import app.dearobjet.backend.domain.classes.dto.CreateClassReservationRequest;
import app.dearobjet.backend.domain.classes.dto.MyClassReservationListResponse;
import app.dearobjet.backend.domain.classes.dto.UpdateClassReservationRequest;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.repository.ShopRepository;
import app.dearobjet.backend.domain.user.repository.UserRepository;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.ErrorCode;
import app.dearobjet.backend.global.exception.InvalidInputException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassReservationService {
    private static final String DEFAULT_RESERVATION_STATUS = "RESERVED";

    private final ClassReservationRepository classReservationRepository;
    private final ClassesRepository classesRepository;
    private final ShopRepository shopRepository;
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
    public ClassReservationResponse createReservation(Long userId, CreateClassReservationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));
        Classes classes = getClassOrThrow(request.getClassId());

        validateGuestCount(classes, request.getGuestCount());
        validateCapacity(classes, request.getReservationTime(), request.getGuestCount(), null);

        ClassReservation reservation = ClassReservation.builder()
                .user(user)
                .classes(classes)
                .guestCount(request.getGuestCount())
                .reservationTime(request.getReservationTime())
                .reservationStatus(DEFAULT_RESERVATION_STATUS)
                .build();

        return toUserResponse(classReservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public MyClassReservationListResponse getMyReservations(Long userId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                size,
                Sort.by(Sort.Direction.DESC, "reservationId")
        );

        Page<ClassReservation> reservationPage = (status == null || status.isBlank())
                ? classReservationRepository.findByUser_Id(userId, pageable)
                : classReservationRepository.findByUser_IdAndReservationStatus(userId, status, pageable);

        List<MyClassReservationListResponse.Item> items = new ArrayList<>();
        for (ClassReservation reservation : reservationPage.getContent()) {
            items.add(new MyClassReservationListResponse.Item(
                    reservation.getReservationId(),
                    reservation.getClasses().getClassesId(),
                    reservation.getClasses().getClassName(),
                    reservation.getClasses().getShop() == null ? null : reservation.getClasses().getShop().getShopName(),
                    reservation.getGuestCount(),
                    reservation.getReservationTime(),
                    reservation.getReservationStatus()
            ));
        }

        return new MyClassReservationListResponse(
                reservationPage.getTotalElements(),
                items,
                page,
                reservationPage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public ClassReservationResponse getMyReservation(Long userId, Long reservationId) {
        return toUserResponse(getMyReservationEntity(userId, reservationId));
    }

    @Transactional
    public ClassReservationResponse updateReservation(
            Long userId,
            Long reservationId,
            UpdateClassReservationRequest request
    ) {
        ClassReservation reservation = getMyReservationEntity(userId, reservationId);
        Classes classes = getClassOrThrow(request.getClassId());

        validateGuestCount(classes, request.getGuestCount());
        validateCapacity(classes, request.getReservationTime(), request.getGuestCount(), reservation);

        reservation.updateReservation(classes, request.getGuestCount(), request.getReservationTime());
        return toUserResponse(reservation);
    }

    @Transactional
    public void deleteReservation(Long userId, Long reservationId) {
        ClassReservation reservation = getMyReservationEntity(userId, reservationId);
        classReservationRepository.delete(reservation);
    }

    private ClassReservation getMyReservationEntity(Long userId, Long reservationId) {
        return classReservationRepository.findByReservationIdAndUser_Id(reservationId, userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "예약을 찾을 수 없습니다."));
    }

    private Classes getClassOrThrow(Long classId) {
        return classesRepository.findById(classId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "클래스를 찾을 수 없습니다."));
    }

    private void validateGuestCount(Classes classes, Integer guestCount) {
        if (guestCount == null || guestCount < 1) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "예약 인원은 1명 이상이어야 합니다.");
        }
        if (classes.getMaxCapacity() != null && guestCount > classes.getMaxCapacity()) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "예약 인원이 클래스 정원을 초과했습니다.");
        }
    }

    private void validateCapacity(
            Classes classes,
            LocalDateTime reservationTime,
            Integer guestCount,
            ClassReservation currentReservation
    ) {
        int reservedGuests = classReservationRepository.sumGuestCountByClasses_ClassesIdAndReservationTime(
                classes.getClassesId(),
                reservationTime
        );

        if (currentReservation != null
                && classes.getClassesId().equals(currentReservation.getClasses().getClassesId())
                && reservationTime.equals(currentReservation.getReservationTime())) {
            reservedGuests -= currentReservation.getGuestCount() == null ? 0 : currentReservation.getGuestCount();
        }

        if (classes.getMaxCapacity() != null && reservedGuests + guestCount > classes.getMaxCapacity()) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "해당 시간 예약 가능 인원을 초과했습니다.");
        }
    }

    private ClassReservationResponse toUserResponse(ClassReservation reservation) {
        return new ClassReservationResponse(
                reservation.getReservationId(),
                reservation.getClasses().getClassesId(),
                reservation.getClasses().getClassName(),
                reservation.getGuestCount(),
                reservation.getReservationTime(),
                reservation.getReservationStatus(),
                reservation.getClasses().getShop() == null ? null : reservation.getClasses().getShop().getShopName()
        );
    }
}
