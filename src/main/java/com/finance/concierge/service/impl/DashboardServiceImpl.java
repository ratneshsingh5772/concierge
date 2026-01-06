package com.finance.concierge.service.impl;

import com.finance.concierge.dto.DashboardStatsDTO;
import com.finance.concierge.dto.ExpenseResponseDTO;
import com.finance.concierge.entity.Category;
import com.finance.concierge.entity.Expense;
import com.finance.concierge.repository.CategoryRepository;
import com.finance.concierge.repository.ExpenseRepository;
import com.finance.concierge.service.BudgetService;
import com.finance.concierge.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of DashboardService for analytics and visualization
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetService budgetService;

    /**
     * Get default budget limits (fallback)
     */
    private Map<String, Double> getDefaultBudgetLimits() {
        return Map.of(
            "Food", 200.0,
            "Transport", 100.0,
            "Entertainment", 150.0,
            "Bills", 300.0,
            "Shopping", 250.0,
            "Health", 200.0,
            "Education", 150.0,
            "Other", 100.0
        );
    }

    @Override
    public DashboardStatsDTO getDashboardStats(Long userId) {
        log.info("Generating dashboard stats for user: {}", userId);

        LocalDate startOfMonth = YearMonth.now().atDay(1);
        LocalDate endOfMonth = YearMonth.now().atEndOfMonth();

        LocalDate startOfLastMonth = YearMonth.now().minusMonths(1).atDay(1);
        LocalDate endOfLastMonth = YearMonth.now().minusMonths(1).atEndOfMonth();

        // Get current month expenses
        List<Expense> currentMonthExpenses = expenseRepository.findByUserIdAndDateBetween(
            userId, startOfMonth, endOfMonth
        );

        // Get last month expenses
        List<Expense> lastMonthExpenses = expenseRepository.findByUserIdAndDateBetween(
            userId, startOfLastMonth, endOfLastMonth
        );

        // Calculate totals
        BigDecimal totalThisMonth = calculateTotal(currentMonthExpenses);
        BigDecimal totalLastMonth = calculateTotal(lastMonthExpenses);

        // Calculate month-over-month change
        Double monthOverMonthChange = calculatePercentageChange(totalLastMonth, totalThisMonth);

        // Generate category breakdown
        List<DashboardStatsDTO.CategorySummary> categoryBreakdown =
            generateCategoryBreakdown(currentMonthExpenses, totalThisMonth);

        // Generate daily spending (last 30 days)
        List<DashboardStatsDTO.DailySpending> dailySpending =
            generateDailySpending(userId, LocalDate.now().minusDays(30), LocalDate.now());

        // Get top 5 expenses
        List<ExpenseResponseDTO> topExpenses = currentMonthExpenses.stream()
            .sorted(Comparator.comparing(Expense::getAmount).reversed())
            .limit(5)
            .map(this::toExpenseResponseDTO)
            .collect(Collectors.toList());

        // Generate budget status
        List<DashboardStatsDTO.BudgetStatus> budgetStatus =
            generateBudgetStatus(currentMonthExpenses);

        return DashboardStatsDTO.builder()
            .totalSpentThisMonth(totalThisMonth)
            .totalSpentLastMonth(totalLastMonth)
            .monthOverMonthChange(monthOverMonthChange)
            .transactionCount(currentMonthExpenses.size())
            .categoryBreakdown(categoryBreakdown)
            .dailySpending(dailySpending)
            .topExpenses(topExpenses)
            .budgetStatus(budgetStatus)
            .build();
    }

    @Override
    public DashboardStatsDTO getDashboardStatsForDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("Generating dashboard stats for user: {} from {} to {}", userId, startDate, endDate);

        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
        BigDecimal total = calculateTotal(expenses);

        return DashboardStatsDTO.builder()
            .totalSpentThisMonth(total)
            .transactionCount(expenses.size())
            .categoryBreakdown(generateCategoryBreakdown(expenses, total))
            .dailySpending(generateDailySpending(userId, startDate, endDate))
            .topExpenses(expenses.stream()
                .sorted(Comparator.comparing(Expense::getAmount).reversed())
                .limit(5)
                .map(this::toExpenseResponseDTO)
                .collect(Collectors.toList()))
            .build();
    }

    @Override
    public DashboardStatsDTO.CategorySummary[] getMonthlyComparison(Long userId, int months) {
        // Implementation for monthly comparison
        return new DashboardStatsDTO.CategorySummary[0];
    }

    /**
     * Calculate total amount from expense list
     */
    private BigDecimal calculateTotal(List<Expense> expenses) {
        return expenses.stream()
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate percentage change between two values
     */
    private Double calculatePercentageChange(BigDecimal oldValue, BigDecimal newValue) {
        if (oldValue.compareTo(BigDecimal.ZERO) == 0) {
            return newValue.compareTo(BigDecimal.ZERO) == 0 ? 0.0 : 100.0;
        }

        BigDecimal change = newValue.subtract(oldValue);
        BigDecimal percentageChange = change.divide(oldValue, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));

        return percentageChange.doubleValue();
    }

    /**
     * Generate category breakdown for pie chart
     */
    private List<DashboardStatsDTO.CategorySummary> generateCategoryBreakdown(
        List<Expense> expenses, BigDecimal total
    ) {
        Map<String, List<Expense>> expensesByCategory = expenses.stream()
            .collect(Collectors.groupingBy(e -> e.getCategory().getName()));

        return expensesByCategory.entrySet().stream()
            .map(entry -> {
                String categoryName = entry.getKey();
                List<Expense> categoryExpenses = entry.getValue();
                BigDecimal categoryTotal = calculateTotal(categoryExpenses);

                Category category = categoryExpenses.get(0).getCategory();

                Double percentage = total.compareTo(BigDecimal.ZERO) == 0 ? 0.0 :
                    categoryTotal.divide(total, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue();

                return DashboardStatsDTO.CategorySummary.builder()
                    .categoryName(categoryName)
                    .categoryIcon(category.getIcon())
                    .categoryColor(category.getColor())
                    .totalAmount(categoryTotal)
                    .transactionCount(categoryExpenses.size())
                    .percentage(percentage)
                    .build();
            })
            .sorted(Comparator.comparing(DashboardStatsDTO.CategorySummary::getTotalAmount).reversed())
            .collect(Collectors.toList());
    }

    /**
     * Generate daily spending data for line chart
     */
    private List<DashboardStatsDTO.DailySpending> generateDailySpending(
        Long userId, LocalDate startDate, LocalDate endDate
    ) {
        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);

        Map<LocalDate, List<Expense>> expensesByDate = expenses.stream()
            .collect(Collectors.groupingBy(Expense::getExpenseDate));

        List<DashboardStatsDTO.DailySpending> dailySpending = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<Expense> dayExpenses = expensesByDate.getOrDefault(date, Collections.emptyList());
            BigDecimal dayTotal = calculateTotal(dayExpenses);

            dailySpending.add(DashboardStatsDTO.DailySpending.builder()
                .date(date.toString())
                .amount(dayTotal)
                .transactionCount(dayExpenses.size())
                .build());
        }

        return dailySpending;
    }

    /**
     * Generate budget status for all categories
     */
    private List<DashboardStatsDTO.BudgetStatus> generateBudgetStatus(List<Expense> expenses) {
        Map<String, BigDecimal> spentByCategory = expenses.stream()
            .collect(Collectors.groupingBy(
                e -> e.getCategory().getName(),
                Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
            ));

        // Get dynamic budget limits from BudgetService
        Long userId = expenses.isEmpty() ? null : expenses.get(0).getUser().getId();
        Map<String, Double> budgetLimits = userId != null ?
            budgetService.getBudgetLimitsMap(userId) :
            getDefaultBudgetLimits();

        return budgetLimits.entrySet().stream()
            .map(entry -> {
                String categoryName = entry.getKey();
                BigDecimal budgetLimit = BigDecimal.valueOf(entry.getValue());
                BigDecimal spent = spentByCategory.getOrDefault(categoryName, BigDecimal.ZERO);
                BigDecimal remaining = budgetLimit.subtract(spent);

                Double percentageUsed = budgetLimit.compareTo(BigDecimal.ZERO) == 0 ? 0.0 :
                    spent.divide(budgetLimit, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue();

                // Get category icon from database
                String icon = getCategoryIcon(categoryName);

                return DashboardStatsDTO.BudgetStatus.builder()
                    .categoryName(categoryName)
                    .categoryIcon(icon)
                    .budgetLimit(budgetLimit)
                    .spent(spent)
                    .remaining(remaining)
                    .percentageUsed(percentageUsed)
                    .isOverBudget(spent.compareTo(budgetLimit) > 0)
                    .build();
            })
            .sorted(Comparator.comparing(DashboardStatsDTO.BudgetStatus::getPercentageUsed).reversed())
            .collect(Collectors.toList());
    }

    /**
     * Get category icon from database
     */
    private String getCategoryIcon(String categoryName) {
        return categoryRepository.findByName(categoryName)
            .map(Category::getIcon)
            .orElse("📦");
    }

    /**
     * Convert Expense entity to DTO
     */
    private ExpenseResponseDTO toExpenseResponseDTO(Expense expense) {
        return ExpenseResponseDTO.builder()
            .id(expense.getId())
            .amount(expense.getAmount())
            .currency(expense.getCurrency())
            .categoryName(expense.getCategory().getName())
            .categoryIcon(expense.getCategory().getIcon())
            .description(expense.getDescription())
            .expenseDate(expense.getExpenseDate())
            .build();
    }
}

