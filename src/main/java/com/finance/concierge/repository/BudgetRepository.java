package com.finance.concierge.repository;

import com.finance.concierge.entity.Budget;
import com.finance.concierge.entity.Budget.BudgetPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Budget entity
 */
@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    /**
     * Find all active budgets for a user
     */
    List<Budget> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * Find budget by user and category
     */
    Optional<Budget> findByUserIdAndCategoryIdAndBudgetPeriodAndIsActiveTrue(
        Long userId, Long categoryId, BudgetPeriod period
    );

    /**
     * Find total budget for user
     */
    Optional<Budget> findByUserIdAndIsTotalBudgetTrueAndBudgetPeriodAndIsActiveTrue(
        Long userId, BudgetPeriod period
    );

    /**
     * Find all category budgets for a user (excluding total budget)
     */
    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND b.isTotalBudget = false AND b.isActive = true AND b.budgetPeriod = :period")
    List<Budget> findCategoryBudgets(@Param("userId") Long userId, @Param("period") BudgetPeriod period);

    /**
     * Check if budget exists for user and category
     */
    boolean existsByUserIdAndCategoryIdAndBudgetPeriodAndIsActiveTrue(
        Long userId, Long categoryId, BudgetPeriod period
    );

    /**
     * Delete budget by user and category
     */
    void deleteByUserIdAndCategoryIdAndBudgetPeriod(Long userId, Long categoryId, BudgetPeriod period);
}

