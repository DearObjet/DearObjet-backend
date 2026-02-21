package app.dearobjet.backend.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OrderStatsResponse {

    private List<SalesPoint> sales;
    private List<PopularItemPoint> popularItems;
    private List<OrdersByHourPoint> ordersByHour;

    @Getter
    @AllArgsConstructor
    public static class SalesPoint {
        private String date;
        private long orderCount;
        private long amount;
    }

    @Getter
    @AllArgsConstructor
    public static class PopularItemPoint {
        private Long itemId;
        private long quantity;
        private long salesAmount;
    }

    @Getter
    @AllArgsConstructor
    public static class OrdersByHourPoint {
        private int hour;
        private long orderCount;
    }
}