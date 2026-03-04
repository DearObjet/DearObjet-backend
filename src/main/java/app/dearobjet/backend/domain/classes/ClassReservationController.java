package app.dearobjet.backend.domain.classes;

import app.dearobjet.backend.domain.classes.dto.UserReservationHistoryResponse;
import app.dearobjet.backend.global.api.ApiResponse;
import app.dearobjet.backend.global.auth.security.CustomUserDetails;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/")
public class ClassReservationController {

    private final ClassReservationService classReservationService;

    @GetMapping("/reservations/me")
    public ApiResponse<List<UserReservationHistoryResponse>> getMyReservations(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.of(classReservationService.getMyReservations(userDetails.getUserId()));
    }
}
