package com.finance.concierge.service;

import java.util.Map;

/**
 * Service interface for finance agent tool functions
 * This wraps the static FinanceAgent methods to enable database integration
 */
public interface FinanceAgentToolService {

    /**
     * Logs a new expense to both CSV and database
     *
     * @param amount The amount spent
     * @param category The expense category
     * @param description A brief description
     * @param userId The user ID (from security context or request)
     * @return Result map with success/error message
     */
    Map<String, String> logExpense(double amount, String category, String description, Long userId);

    /**
     * Checks budget status for a category
     *
     * @param category The category to check
     * @param userId The user ID
     * @return Budget status information
     */
    Map<String, String> getBudgetStatus(String category, Long userId);

    /**
     * Creates a monthly expense report
     *
     * @param userId The user ID
     * @return Monthly report data
     */
    Map<String, Object> createMonthlyReport(Long userId);
}

