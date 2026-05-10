package app.dearobjet.backend.domain.classes;

import app.dearobjet.backend.domain.classes.dto.AvailableClassSlotsResponse;
import app.dearobjet.backend.domain.classes.dto.ClassReservationListResponse;
import app.dearobjet.backend.domain.classes.dto.CreateClassReservationRequest;
import app.dearobjet.backend.domain.classes.dto.CreateClassReservationResponse;
import app.dearobjet.backend.domain.classes.dto.MyClassReservationsResponse;
import app.dearobjet.backend.global.api.ApiResponse;
import app.dearobjet.backend.global.auth.security.CustomUserDetails;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ClassReservationController {

    private final ClassReservationService classReservationService;

    @GetMapping("/class-reservations")
    public ApiResponse<ClassReservationListResponse> getReservations(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.of(
                classReservationService.getReservations(userDetails.getUserId(), status, page, size)
        );
    }

    @GetMapping("/class-reservations/me")
    public ApiResponse<MyClassReservationsResponse> getMyReservations(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.of(classReservationService.getMyReservations(userDetails.getUserId()));
    }

    @PostMapping("/class-reservations")
    public ApiResponse<CreateClassReservationResponse> createReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateClassReservationRequest request
    ) {
        return ApiResponse.of(classReservationService.createReservation(userDetails.getUserId(), request));
    }

    @PostMapping("/class-reservations/{reservationId}/change")
    public ApiResponse<CreateClassReservationResponse> changeReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reservationId,
            @Valid @RequestBody CreateClassReservationRequest request
    ) {
        return ApiResponse.of(
                classReservationService.changeReservation(userDetails.getUserId(), reservationId, request)
        );
    }

    @PostMapping("/class-reservations/{reservationId}/cancel")
    public ApiResponse<Void> cancelReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reservationId
    ) {
        classReservationService.cancelReservation(userDetails.getUserId(), reservationId);
        return ApiResponse.of(null);
    }

    @GetMapping("/classes/{classId}/available-slots")
    public ApiResponse<AvailableClassSlotsResponse> getAvailableSlots(
            @PathVariable Long classId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.of(classReservationService.getAvailableSlots(classId, date));
    }
}
