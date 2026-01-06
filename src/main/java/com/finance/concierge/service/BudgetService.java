package com.finance.concierge.service;

import com.finance.concierge.dto.BudgetRequestDTO;
import com.finance.concierge.dto.BudgetResponseDTO;
import com.finance.concierge.entity.Budget;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Service interface for Budget operations
 */
public interface BudgetService {

    /**
     * Set or update budget for a category
     */
    BudgetResponseDTO setBudget(Long userId, BudgetRequestDTO request);

    /**
     * Set or update total budget
     */
    BudgetResponseDTO setTotalBudget(Long userId, BudgetRequestDTO request);

    /**
     * Get all budgets for user
     */
    List<BudgetResponseDTO> getAllBudgets(Long userId, String period);

    /**
     * Get budget by category
     */
    BudgetResponseDTO getBudgetByCategory(Long userId, String categoryName, String period);

    /**
     * Get total budget
     */
    BudgetResponseDTO getTotalBudget(Long userId, String period);

    /**
     * Delete budget
     */
    void deleteBudget(Long userId, Long budgetId);

    /**
     * Get budget limits as Map (for AI agent)
     */
    Map<String, Double> getBudgetLimitsMap(Long userId);

    /**
     * Check if spending exceeds budget
     */
    boolean isOverBudget(Long userId, String categoryName, String period);

    /**
     * Get remaining budget for category
     */
    BigDecimal getRemainingBudget(Long userId, String categoryName, String period);
}

