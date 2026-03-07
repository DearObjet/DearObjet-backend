package app.dearobjet.backend.domain.settlement;

import app.dearobjet.backend.domain.settlement.dto.SettlementMonthlyForecastResponse;
import app.dearobjet.backend.domain.settlement.dto.projection.RecentSettlementArtistRow;
import app.dearobjet.backend.domain.settlement.dto.projection.SettlementMonthlySummaryRow;
import app.dearobjet.backend.domain.user.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final ShopRepository shopRepository;

    @Transactional(readOnly = true)
    public SettlementMonthlyForecastResponse getCurrentMonthForecast(Long userId) {
        shopRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found for user: " + userId));

        LocalDate now = LocalDate.now();
        LocalDateTime startInclusive = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endExclusive = now.plusMonths(1).withDayOfMonth(1).atStartOfDay();

        SettlementMonthlySummaryRow summary = settlementRepository.findMonthlyForecastByUserId(
                userId, startInclusive, endExclusive
        );

        double expectedShopAmount = (summary == null || summary.getExpectedShopAmount() == null)
                ? 0D
                : summary.getExpectedShopAmount();
        long settlementCount = (summary == null || summary.getSettlementCount() == null)
                ? 0L
                : summary.getSettlementCount();
        List<SettlementMonthlyForecastResponse.RecentSettlement> recentSettlements = settlementRepository.findRecentSettlementsByUserId(
                userId, startInclusive, endExclusive
        ).stream().map(row -> new SettlementMonthlyForecastResponse.RecentSettlement(
                row.getArtistName(),
                row.getAmount() == null ? 0D : row.getAmount()
        )).toList();

        return new SettlementMonthlyForecastResponse(
                now.getYear(),
                now.getMonthValue(),
                expectedShopAmount,
                settlementCount,
                recentSettlements
        );
    }
}
