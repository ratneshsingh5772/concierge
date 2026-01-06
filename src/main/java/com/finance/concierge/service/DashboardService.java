package com.finance.concierge.service;

import com.finance.concierge.dto.DashboardStatsDTO;

import java.time.LocalDate;

/**
 * Service for generating dashboard statistics and analytics
 */
public interface DashboardService {

    /**
     * Get comprehensive dashboard statistics for the current month
     *
     * @param userId The user ID
     * @return Dashboard statistics including totals, breakdowns, and charts data
     */
    DashboardStatsDTO getDashboardStats(Long userId);

    /**
     * Get dashboard statistics for a specific date range
     *
     * @param userId The user ID
     * @param startDate Start date
     * @param endDate End date
     * @return Dashboard statistics for the date range
     */
    DashboardStatsDTO getDashboardStatsForDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Get monthly comparison (current month vs previous months)
     *
     * @param userId The user ID
     * @param months Number of months to include
     * @return Monthly comparison data
     */
    DashboardStatsDTO.CategorySummary[] getMonthlyComparison(Long userId, int months);
}

