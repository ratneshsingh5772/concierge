package com.finance.concierge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO for dashboard statistics and data visualization
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {

    /**
     * Current month total spending
     */
    private BigDecimal totalSpentThisMonth;

    /**
     * Previous month total spending
     */
    private BigDecimal totalSpentLastMonth;

    /**
     * Percentage change from last month
     */
    private Double monthOverMonthChange;

    /**
     * Total number of transactions this month
     */
    private Integer transactionCount;

    /**
     * Category breakdown for pie chart
     */
    private List<CategorySummary> categoryBreakdown;

    /**
     * Daily spending for line chart (last 30 days)
     */
    private List<DailySpending> dailySpending;

    /**
     * Top 5 most expensive transactions
     */
    private List<ExpenseResponseDTO> topExpenses;

    /**
     * Budget status for each category
     */
    private List<BudgetStatus> budgetStatus;

    /**
     * Category summary for visualization
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySummary {
        private String categoryName;
        private String categoryIcon;
        private String categoryColor;
        private BigDecimal totalAmount;
        private Integer transactionCount;
        private Double percentage;
    }

    /**
     * Daily spending data for line chart
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailySpending {
        private String date;
        private BigDecimal amount;
        private Integer transactionCount;
    }

    /**
     * Budget status for progress bars
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BudgetStatus {
        private String categoryName;
        private String categoryIcon;
        private BigDecimal budgetLimit;
        private BigDecimal spent;
        private BigDecimal remaining;
        private Double percentageUsed;
        private Boolean isOverBudget;
    }
}

