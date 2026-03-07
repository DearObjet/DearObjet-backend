package app.dearobjet.backend.domain.settlement;

import app.dearobjet.backend.domain.settlement.dto.SettlementMonthlyForecastResponse;
import app.dearobjet.backend.global.api.ApiResponse;
import app.dearobjet.backend.global.auth.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping("/forecast/current-month")
    public ApiResponse<SettlementMonthlyForecastResponse> getCurrentMonthForecast(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.of(settlementService.getCurrentMonthForecast(userDetails.getUserId()));
    }
}
