package app.dearobjet.backend.domain.classes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import app.dearobjet.backend.domain.classes.dto.AvailableClassSlotsResponse;
import app.dearobjet.backend.domain.classes.dto.CreateClassReservationRequest;
import app.dearobjet.backend.domain.classes.dto.CreateClassReservationResponse;
import app.dearobjet.backend.domain.classes.dto.MyClassReservationsResponse;
import app.dearobjet.backend.domain.shop.entity.Shop;
import app.dearobjet.backend.domain.shop.entity.ShopBusinessHour;
import app.dearobjet.backend.domain.shop.repository.ShopBusinessHourRepository;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.repository.ShopRepository;
import app.dearobjet.backend.domain.user.repository.UserRepository;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.InvalidInputException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClassReservationServiceTest {

    @Mock
    private ClassReservationRepository classReservationRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private ClassesRepository classesRepository;

    @Mock
    private ClassSessionRepository classSessionRepository;

    @Mock
    private ShopBusinessHourRepository shopBusinessHourRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ClassReservationService classReservationService;

    @Test
    void getAvailableSlots_returnsHourlySlotsExcludingFullReservations() {
        LocalDate date = LocalDate.now().plusDays(1);
        Shop shop = Shop.builder().shopId(1L).build();
        Classes classes = Classes.builder()
                .classesId(10L)
                .shop(shop)
                .maxCapacity(4)
                .build();
        ShopBusinessHour businessHour = ShopBusinessHour.builder()
                .shop(shop)
                .dayOfWeek(date.getDayOfWeek())
                .openMinutes(600)
                .closeMinutes(780)
                .build();
        ClassSession session10 = ClassSession.builder()
                .sessionId(100L)
                .classes(classes)
                .shop(shop)
                .startDatetime(date.atTime(10, 0))
                .endDatetime(date.atTime(11, 0))
                .capacity(4)
                .sessionStatus("OPEN")
                .build();
        ClassSession session11 = ClassSession.builder()
                .sessionId(101L)
                .classes(classes)
                .shop(shop)
                .startDatetime(date.atTime(11, 0))
                .endDatetime(date.atTime(12, 0))
                .capacity(4)
                .sessionStatus("OPEN")
                .build();
        ClassSession session12 = ClassSession.builder()
                .sessionId(102L)
                .classes(classes)
                .shop(shop)
                .startDatetime(date.atTime(12, 0))
                .endDatetime(date.atTime(13, 0))
                .capacity(4)
                .sessionStatus("OPEN")
                .build();

        ClassReservation fullReservation = ClassReservation.builder()
                .reservationTime(date.atTime(11, 0))
                .guestCount(4)
                .classes(classes)
                .build();
        ClassReservation partialReservation = ClassReservation.builder()
                .reservationTime(date.atTime(10, 0))
                .guestCount(1)
                .classes(classes)
                .build();

        given(classesRepository.findById(10L)).willReturn(Optional.of(classes));
        given(shopBusinessHourRepository.findByShopAndDayOfWeek(shop, date.getDayOfWeek()))
                .willReturn(Optional.of(businessHour));
        given(classSessionRepository.findByClasses_ClassesIdAndStartDatetimeBetweenOrderByStartDatetimeAsc(
                10L,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay().minusNanos(1)
        )).willReturn(List.of(session10, session11, session12));
        given(classReservationRepository.findByClasses_ClassesIdAndReservationTimeBetween(
                10L,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay().minusNanos(1)
        )).willReturn(List.of(fullReservation, partialReservation));

        AvailableClassSlotsResponse response = classReservationService.getAvailableSlots(10L, date);

        assertThat(response.openTime()).isEqualTo("10:00");
        assertThat(response.closeTime()).isEqualTo("13:00");
        assertThat(response.slots()).hasSize(3);
        assertThat(response.slots())
                .extracting(AvailableClassSlotsResponse.Slot::time)
                .containsExactly("10:00", "11:00", "12:00");
        assertThat(response.slots())
                .extracting(AvailableClassSlotsResponse.Slot::sessionId)
                .containsExactly(100L, 101L, 102L);
        assertThat(response.slots().get(0).remainingCapacity()).isEqualTo(3);
        assertThat(response.slots().get(1).remainingCapacity()).isEqualTo(0);
        assertThat(response.slots().get(1).available()).isFalse();
        assertThat(response.slots().get(2).remainingCapacity()).isEqualTo(4);
    }

    @Test
    void getAvailableSlots_returnsEmptyWhenBusinessHourMissing() {
        LocalDate date = LocalDate.now().plusDays(1);
        Shop shop = Shop.builder().shopId(1L).build();
        Classes classes = Classes.builder()
                .classesId(10L)
                .shop(shop)
                .build();

        given(classesRepository.findById(10L)).willReturn(Optional.of(classes));
        given(shopBusinessHourRepository.findByShopAndDayOfWeek(shop, date.getDayOfWeek()))
                .willReturn(Optional.empty());

        AvailableClassSlotsResponse response = classReservationService.getAvailableSlots(10L, date);

        assertThat(response.slots()).isEmpty();
        assertThat(response.openTime()).isNull();
        assertThat(response.closeTime()).isNull();
    }

    @Test
    void getAvailableSlots_throwsWhenClassMissing() {
        LocalDate date = LocalDate.of(2026, 4, 13);

        given(classesRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> classReservationService.getAvailableSlots(999L, date))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getAvailableSlots_createsSessionsInDatabaseWhenMissing() {
        LocalDate date = LocalDate.now().plusDays(1);
        Shop shop = Shop.builder().shopId(1L).build();
        Classes classes = Classes.builder()
                .classesId(10L)
                .shop(shop)
                .maxCapacity(2)
                .build();
        ShopBusinessHour businessHour = ShopBusinessHour.builder()
                .shop(shop)
                .dayOfWeek(date.getDayOfWeek())
                .openMinutes(600)
                .closeMinutes(720)
                .build();
        ClassSession savedSession10 = ClassSession.builder()
                .sessionId(200L)
                .classes(classes)
                .shop(shop)
                .startDatetime(date.atTime(10, 0))
                .endDatetime(date.atTime(11, 0))
                .capacity(2)
                .sessionStatus("OPEN")
                .build();
        ClassSession savedSession11 = ClassSession.builder()
                .sessionId(201L)
                .classes(classes)
                .shop(shop)
                .startDatetime(date.atTime(11, 0))
                .endDatetime(date.atTime(12, 0))
                .capacity(2)
                .sessionStatus("OPEN")
                .build();

        given(classesRepository.findById(10L)).willReturn(Optional.of(classes));
        given(shopBusinessHourRepository.findByShopAndDayOfWeek(shop, date.getDayOfWeek()))
                .willReturn(Optional.of(businessHour));
        given(classSessionRepository.findByClasses_ClassesIdAndStartDatetimeBetweenOrderByStartDatetimeAsc(
                10L,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay().minusNanos(1)
        )).willReturn(List.of());
        given(classSessionRepository.saveAll(org.mockito.ArgumentMatchers.anyList()))
                .willReturn(List.of(savedSession10, savedSession11));
        given(classReservationRepository.findByClasses_ClassesIdAndReservationTimeBetween(
                10L,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay().minusNanos(1)
        )).willReturn(List.of());

        AvailableClassSlotsResponse response = classReservationService.getAvailableSlots(10L, date);

        assertThat(response.slots()).hasSize(2);
        assertThat(response.slots())
                .extracting(AvailableClassSlotsResponse.Slot::sessionId)
                .containsExactly(200L, 201L);
    }

    @Test
    void createReservation_savesReservationWhenCapacityRemains() {
        LocalDate date = LocalDate.now().plusDays(1);
        Shop shop = Shop.builder().shopId(1L).build();
        User user = User.builder().id(1L).name("예약자").build();
        Classes classes = Classes.builder()
                .classesId(10L)
                .shop(shop)
                .maxCapacity(4)
                .build();
        ClassSession session = ClassSession.builder()
                .sessionId(100L)
                .classes(classes)
                .shop(shop)
                .startDatetime(date.atTime(10, 0))
                .endDatetime(date.atTime(11, 0))
                .capacity(4)
                .sessionStatus("OPEN")
                .build();
        CreateClassReservationRequest request = new CreateClassReservationRequest(
                100L,
                2,
                "홍길동",
                "창가 자리면 좋아요"
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(classSessionRepository.findBySessionId(100L))
                .willReturn(Optional.of(session));
        given(classReservationRepository.existsByUser_IdAndReservationStatus(
                eq(1L),
                eq(ClassReservationStatus.PENDING)
        )).willReturn(false);
        given(classReservationRepository.sumGuestCountBySessionId(100L)).willReturn(1);
        given(classReservationRepository.save(org.mockito.ArgumentMatchers.any(ClassReservation.class)))
                .willAnswer(invocation -> {
                    ClassReservation reservation = invocation.getArgument(0);
                    return ClassReservation.builder()
                            .reservationId(999L)
                            .guestCount(reservation.getGuestCount())
                            .reservationTime(reservation.getReservationTime())
                            .reservationStatus(reservation.getReservationStatus())
                            .reservationName(reservation.getReservationName())
                            .memo(reservation.getMemo())
                            .user(reservation.getUser())
                            .classes(reservation.getClasses())
                            .classSession(reservation.getClassSession())
                            .build();
                });

        CreateClassReservationResponse response = classReservationService.createReservation(1L, request);

        assertThat(response.reservationId()).isEqualTo(999L);
        assertThat(response.reservationStatus()).isEqualTo("PENDING");
    }

    @Test
    void createReservation_throwsWhenCapacityExceeded() {
        LocalDate date = LocalDate.now().plusDays(1);
        Shop shop = Shop.builder().shopId(1L).build();
        User user = User.builder().id(1L).name("예약자").build();
        Classes classes = Classes.builder()
                .classesId(10L)
                .shop(shop)
                .maxCapacity(4)
                .build();
        ClassSession session = ClassSession.builder()
                .sessionId(100L)
                .classes(classes)
                .shop(shop)
                .startDatetime(date.atTime(10, 0))
                .endDatetime(date.atTime(11, 0))
                .capacity(4)
                .sessionStatus("OPEN")
                .build();
        CreateClassReservationRequest request = new CreateClassReservationRequest(
                100L,
                2,
                "홍길동",
                null
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(classSessionRepository.findBySessionId(100L))
                .willReturn(Optional.of(session));
        given(classReservationRepository.existsByUser_IdAndReservationStatus(
                eq(1L),
                eq(ClassReservationStatus.PENDING)
        )).willReturn(false);
        given(classReservationRepository.sumGuestCountBySessionId(100L)).willReturn(3);

        assertThatThrownBy(() -> classReservationService.createReservation(1L, request))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void createReservation_throwsWhenRequesterIsClassOwner() {
        LocalDate date = LocalDate.now().plusDays(1);
        User owner = User.builder().id(1L).name("개설자").build();
        Shop shop = Shop.builder().shopId(1L).user(owner).build();
        Classes classes = Classes.builder()
                .classesId(10L)
                .shop(shop)
                .maxCapacity(4)
                .build();
        ClassSession session = ClassSession.builder()
                .sessionId(100L)
                .classes(classes)
                .shop(shop)
                .startDatetime(date.atTime(10, 0))
                .endDatetime(date.atTime(11, 0))
                .capacity(4)
                .sessionStatus("OPEN")
                .build();
        CreateClassReservationRequest request = new CreateClassReservationRequest(
                100L,
                1,
                "개설자",
                null
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(owner));
        given(classSessionRepository.findBySessionId(100L))
                .willReturn(Optional.of(session));
        given(classReservationRepository.existsByUser_IdAndReservationStatus(
                eq(1L),
                eq(ClassReservationStatus.PENDING)
        )).willReturn(false);

        assertThatThrownBy(() -> classReservationService.createReservation(1L, request))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("클래스를 개설한 본인은 예약할 수 없습니다.");
    }

    @Test
    void createReservation_throwsWhenActiveReservationAlreadyExists() {
        LocalDate date = LocalDate.now().plusDays(1);
        Shop shop = Shop.builder().shopId(1L).build();
        User user = User.builder().id(1L).name("예약자").build();
        Classes classes = Classes.builder()
                .classesId(10L)
                .shop(shop)
                .maxCapacity(4)
                .build();
        ClassSession session = ClassSession.builder()
                .sessionId(100L)
                .classes(classes)
                .shop(shop)
                .startDatetime(date.atTime(10, 0))
                .endDatetime(date.atTime(11, 0))
                .capacity(4)
                .sessionStatus("OPEN")
                .build();
        CreateClassReservationRequest request = new CreateClassReservationRequest(
                100L,
                1,
                "홍길동",
                null
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(classSessionRepository.findBySessionId(100L)).willReturn(Optional.of(session));
        given(classReservationRepository.existsByUser_IdAndReservationStatus(
                eq(1L),
                eq(ClassReservationStatus.PENDING)
        )).willReturn(true);

        assertThatThrownBy(() -> classReservationService.createReservation(1L, request))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("이미 대기 중인 원데이클래스 예약이 있습니다");
    }

    @Test
    void getMyReservations_returnsCurrentAndPastReservations() {
        LocalDateTime now = LocalDateTime.now();
        Shop currentShop = org.mockito.Mockito.mock(Shop.class);
        Shop pastShop = org.mockito.Mockito.mock(Shop.class);
        Classes currentClass = Classes.builder()
                .classesId(10L)
                .shop(currentShop)
                .className("현재 클래스")
                .build();
        Classes pastClass = Classes.builder()
                .classesId(20L)
                .shop(pastShop)
                .className("지난 클래스")
                .build();

        ClassReservation currentReservation = ClassReservation.builder()
                .reservationId(1L)
                .reservationTime(now.plusDays(1))
                .reservationStatus(ClassReservationStatus.PENDING)
                .classes(currentClass)
                .build();
        ClassReservation canceledReservation = ClassReservation.builder()
                .reservationId(2L)
                .reservationTime(now.plusDays(2))
                .reservationStatus(ClassReservationStatus.CANCELED)
                .classes(pastClass)
                .build();
        ClassReservation pastReservation = ClassReservation.builder()
                .reservationId(3L)
                .reservationTime(now.minusDays(1))
                .reservationStatus(ClassReservationStatus.CONFIRMED)
                .classes(pastClass)
                .build();

        given(currentShop.getShopName()).willReturn("현재매장");
        given(pastShop.getShopName()).willReturn("지난매장");
        given(classReservationRepository.findByUser_IdOrderByReservationTimeDesc(1L))
                .willReturn(List.of(canceledReservation, currentReservation, pastReservation));

        MyClassReservationsResponse response = classReservationService.getMyReservations(1L);

        assertThat(response.currentReservations()).hasSize(1);
        assertThat(response.currentReservations().get(0).reservationNumber()).isEqualTo(1L);
        assertThat(response.currentReservations().get(0).status()).isEqualTo("PENDING");
        assertThat(response.currentReservations().get(0).reservationStore()).isEqualTo("현재매장");
        assertThat(response.currentReservations().get(0).className()).isEqualTo("현재 클래스");

        assertThat(response.pastReservations()).hasSize(2);
        assertThat(response.pastReservations())
                .extracting(MyClassReservationsResponse.ReservationSummary::reservationNumber)
                .containsExactly(2L, 3L);
        assertThat(response.pastReservations())
                .extracting(MyClassReservationsResponse.ReservationSummary::status)
                .containsExactly("CANCELED", "CONFIRMED");
    }

    @Test
    void getMyReservations_throwsWhenReservationHistoryMissing() {
        given(classReservationRepository.findByUser_IdOrderByReservationTimeDesc(1L))
                .willReturn(List.of());

        assertThatThrownBy(() -> classReservationService.getMyReservations(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("예약한 원데이클래스 내역이 없습니다");
    }
}
