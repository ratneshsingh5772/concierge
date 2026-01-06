package com.finance.concierge.service;

import com.finance.concierge.dto.ParsedExpenseDTO;
import com.finance.concierge.entity.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for Expense operations
 */
public interface ExpenseService {

    /**
     * Create expense from parsed data
     */
    Expense createExpense(Long userId, ParsedExpenseDTO parsedData);

    /**
     * Create expense from natural language message
     */
    Expense createExpenseFromMessage(String message, Long userId);

    /**
     * Get user's expenses by category
     */
    List<Expense> getExpensesByCategory(Long userId, String categoryName);

    /**
     * Get user's expenses for date range
     */
    List<Expense> getExpensesByDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Calculate total spent by category
     */
    BigDecimal getTotalSpentByCategory(Long userId, String categoryName);

    /**
     * Calculate total spent in date range
     */
    BigDecimal getTotalSpentInDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Calculate total spent by category in date range
     */
    BigDecimal getTotalSpentByCategoryInDateRange(Long userId, String categoryName, LocalDate startDate, LocalDate endDate);

    /**
     * Get current month expenses
     */
    List<Expense> getCurrentMonthExpenses(Long userId);

    /**
     * Get total spent this month
     */
    BigDecimal getTotalSpentThisMonth(Long userId);
}

