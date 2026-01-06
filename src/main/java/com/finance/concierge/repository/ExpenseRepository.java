package com.finance.concierge.repository;

import com.finance.concierge.entity.Expense;
import com.finance.concierge.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Expense entity operations
 */
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /**
     * Find all expenses for a user
     */
    Page<Expense> findByUserOrderByExpenseDateDesc(User user, Pageable pageable);

    /**
     * Find expenses by user ID
     */
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId ORDER BY e.expenseDate DESC")
    Page<Expense> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find expenses by user and category
     */
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND e.category.name = :categoryName ORDER BY e.expenseDate DESC")
    List<Expense> findByUserIdAndCategoryName(@Param("userId") Long userId, @Param("categoryName") String categoryName);

    /**
     * Find expenses by user and date range
     */
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND e.expenseDate BETWEEN :startDate AND :endDate ORDER BY e.expenseDate DESC")
    List<Expense> findByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Find expenses by user and date between (for monthly report)
     */
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND e.expenseDate BETWEEN :startDate AND :endDate ORDER BY e.expenseDate DESC")
    List<Expense> findByUserIdAndDateBetween(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Calculate total amount by user and category
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.category.name = :categoryName")
    BigDecimal sumAmountByUserIdAndCategoryName(@Param("userId") Long userId, @Param("categoryName") String categoryName);

    /**
     * Calculate total amount by user and date range
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.expenseDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Calculate total amount by user, category, and date range
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.category.name = :categoryName AND e.expenseDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserIdCategoryAndDateRange(
        @Param("userId") Long userId,
        @Param("categoryName") String categoryName,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Find AI-parsed expenses
     */
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND e.aiParsed = true ORDER BY e.createdAt DESC")
    List<Expense> findAIParsedExpensesByUserId(@Param("userId") Long userId);

    /**
     * Count expenses by user
     */
    long countByUserId(Long userId);

    /**
     * Find daily spending
     */
    @Query("SELECT e.expenseDate, SUM(e.amount) FROM Expense e WHERE e.user = :user AND e.expenseDate >= :startDate AND e.expenseDate <= :endDate GROUP BY e.expenseDate ORDER BY e.expenseDate ASC")
    List<Object[]> findDailySpending(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Find monthly spending
     */
    @Query("SELECT FUNCTION('MONTH', e.expenseDate), SUM(e.amount) FROM Expense e WHERE e.user = :user AND FUNCTION('YEAR', e.expenseDate) = :year GROUP BY FUNCTION('MONTH', e.expenseDate) ORDER BY FUNCTION('MONTH', e.expenseDate) ASC")
    List<Object[]> findMonthlySpending(@Param("user") User user, @Param("year") int year);

    /**
     * Find total spent in period
     */
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user = :user AND e.expenseDate >= :startDate AND e.expenseDate <= :endDate")
    BigDecimal findTotalSpentInPeriod(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Find highest daily spends
     */
    @Query("SELECT e.expenseDate, SUM(e.amount) as total FROM Expense e WHERE e.user = :user GROUP BY e.expenseDate ORDER BY total DESC")
    List<Object[]> findHighestDailySpends(@Param("user") User user, Pageable pageable);
}
