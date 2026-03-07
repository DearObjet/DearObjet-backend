package app.dearobjet.backend.domain.settlement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SettlementMonthlyForecastResponse {
    private int year;
    private int month;
    private double expectedShopAmount;
    private long settlementCount;
    private List<RecentSettlement> recentSettlements;

    @Getter
    @AllArgsConstructor
    public static class RecentSettlement {
        private String artistName;
        private double amount;
    }
}
