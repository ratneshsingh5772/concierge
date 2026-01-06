package com.finance.concierge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public class AnalyticsDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTrendDTO {
        private String date; // YYYY-MM-DD
        private String day;  // Mon, Tue, etc.
        private BigDecimal amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlySpendDTO {
        private String month; // Jan, Feb, etc.
        private BigDecimal amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HighestDailySpendDTO {
        private String date;
        private BigDecimal amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryDTO {
        private BigDecimal totalSpentLast10Days;
        private BigDecimal projectedMonthlySpend;
        private HighestDailySpendDTO highestDailySpend;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForecastDTO {
        private BigDecimal predictedYearEndSpend;
        private BigDecimal predictedMonthEndSpend;
        private PredictedExpenseDTO nextLikelySpend;
        private String aiAnalysis;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PredictedExpenseDTO {
        private String category;
        private BigDecimal estimatedAmount;
        private String confidence;
    }
}
