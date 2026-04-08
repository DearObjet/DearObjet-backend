package app.dearobjet.backend.domain.classes;

import app.dearobjet.backend.domain.classes.dto.ClassReservationListResponse;
import app.dearobjet.backend.domain.classes.dto.ClassReservationResponse;
import app.dearobjet.backend.domain.classes.dto.CreateClassReservationRequest;
import app.dearobjet.backend.domain.classes.dto.MyClassReservationListResponse;
import app.dearobjet.backend.domain.classes.dto.UpdateClassReservationRequest;
import app.dearobjet.backend.global.api.ApiResponse;
import app.dearobjet.backend.global.auth.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ClassReservationController {

    private final ClassReservationService classReservationService;

    @PostMapping("/class-reservations")
    public ApiResponse<ClassReservationResponse> createReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateClassReservationRequest request
    ) {
        return ApiResponse.of(classReservationService.createReservation(userDetails.getUserId(), request));
    }

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

    @GetMapping("/me/class-reservations")
    public ApiResponse<MyClassReservationListResponse> getMyReservations(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.of(
                classReservationService.getMyReservations(userDetails.getUserId(), status, page, size)
        );
    }

    @GetMapping("/me/class-reservations/{reservationId}")
    public ApiResponse<ClassReservationResponse> getMyReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reservationId
    ) {
        return ApiResponse.of(classReservationService.getMyReservation(userDetails.getUserId(), reservationId));
    }

    @PutMapping("/me/class-reservations/{reservationId}")
    public ApiResponse<ClassReservationResponse> updateReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reservationId,
            @Valid @RequestBody UpdateClassReservationRequest request
    ) {
        return ApiResponse.of(
                classReservationService.updateReservation(userDetails.getUserId(), reservationId, request)
        );
    }

    @DeleteMapping("/me/class-reservations/{reservationId}")
    public ApiResponse<Void> deleteReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reservationId
    ) {
        classReservationService.deleteReservation(userDetails.getUserId(), reservationId);
        return ApiResponse.of(null);
    }
}
