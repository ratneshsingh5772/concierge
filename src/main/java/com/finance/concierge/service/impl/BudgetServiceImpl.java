package com.finance.concierge.service.impl;

import com.finance.concierge.dto.BudgetRequestDTO;
import com.finance.concierge.dto.BudgetResponseDTO;
import com.finance.concierge.entity.Budget;
import com.finance.concierge.entity.Budget.BudgetPeriod;
import com.finance.concierge.entity.Category;
import com.finance.concierge.entity.User;
import com.finance.concierge.repository.BudgetRepository;
import com.finance.concierge.repository.CategoryRepository;
import com.finance.concierge.repository.UserRepository;
import com.finance.concierge.service.BudgetService;
import com.finance.concierge.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of BudgetService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ExpenseService expenseService;

    @Override
    @Transactional
    public BudgetResponseDTO setBudget(Long userId, BudgetRequestDTO request) {
        log.info("Setting budget for user: {}, category: {}, amount: {}",
            userId, request.getCategoryName(), request.getBudgetAmount());

        // Validate and get user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Validate and get category
        Category category = categoryRepository.findByName(request.getCategoryName())
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + request.getCategoryName()));

        BudgetPeriod period = BudgetPeriod.valueOf(request.getBudgetPeriod().toUpperCase());

        // Check if budget already exists
        Budget budget = budgetRepository.findByUserIdAndCategoryIdAndBudgetPeriodAndIsActiveTrue(
            userId, category.getId(), period
        ).orElse(Budget.builder()
            .user(user)
            .category(category)
            .budgetPeriod(period)
            .isTotalBudget(false)
            .isActive(true)
            .build());

        // Update budget
        budget.setBudgetAmount(request.getBudgetAmount());
        budget.setAlertThreshold(request.getAlertThreshold());

        Budget saved = budgetRepository.save(budget);
        log.info("Budget saved with ID: {}", saved.getId());

        return toBudgetResponseDTO(saved, userId);
    }

    @Override
    @Transactional
    public BudgetResponseDTO setTotalBudget(Long userId, BudgetRequestDTO request) {
        log.info("Setting total budget for user: {}, amount: {}", userId, request.getBudgetAmount());

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        BudgetPeriod period = BudgetPeriod.valueOf(request.getBudgetPeriod().toUpperCase());

        // Check if total budget already exists
        Budget budget = budgetRepository.findByUserIdAndIsTotalBudgetTrueAndBudgetPeriodAndIsActiveTrue(
            userId, period
        ).orElse(Budget.builder()
            .user(user)
            .category(null)
            .budgetPeriod(period)
            .isTotalBudget(true)
            .isActive(true)
            .build());

        budget.setBudgetAmount(request.getBudgetAmount());
        budget.setAlertThreshold(request.getAlertThreshold());

        Budget saved = budgetRepository.save(budget);
        log.info("Total budget saved with ID: {}", saved.getId());

        return toBudgetResponseDTO(saved, userId);
    }

    @Override
    public List<BudgetResponseDTO> getAllBudgets(Long userId, String periodStr) {
        log.info("Fetching all budgets for user: {}, period: {}", userId, periodStr);

        BudgetPeriod period = BudgetPeriod.valueOf(periodStr.toUpperCase());
        List<Budget> budgets = budgetRepository.findByUserIdAndIsActiveTrue(userId);

        return budgets.stream()
            .filter(b -> b.getBudgetPeriod() == period)
            .map(b -> toBudgetResponseDTO(b, userId))
            .collect(Collectors.toList());
    }

    @Override
    public BudgetResponseDTO getBudgetByCategory(Long userId, String categoryName, String periodStr) {
        log.info("Fetching budget for user: {}, category: {}", userId, categoryName);

        Category category = categoryRepository.findByName(categoryName)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryName));

        BudgetPeriod period = BudgetPeriod.valueOf(periodStr.toUpperCase());

        Budget budget = budgetRepository.findByUserIdAndCategoryIdAndBudgetPeriodAndIsActiveTrue(
            userId, category.getId(), period
        ).orElseThrow(() -> new IllegalArgumentException(
            "No budget found for category: " + categoryName));

        return toBudgetResponseDTO(budget, userId);
    }

    @Override
    public BudgetResponseDTO getTotalBudget(Long userId, String periodStr) {
        log.info("Fetching total budget for user: {}", userId);

        BudgetPeriod period = BudgetPeriod.valueOf(periodStr.toUpperCase());

        Budget budget = budgetRepository.findByUserIdAndIsTotalBudgetTrueAndBudgetPeriodAndIsActiveTrue(
            userId, period
        ).orElseThrow(() -> new IllegalArgumentException("No total budget set"));

        return toBudgetResponseDTO(budget, userId);
    }

    @Override
    @Transactional
    public void deleteBudget(Long userId, Long budgetId) {
        log.info("Deleting budget: {} for user: {}", budgetId, userId);

        Budget budget = budgetRepository.findById(budgetId)
            .orElseThrow(() -> new IllegalArgumentException("Budget not found: " + budgetId));

        if (!budget.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Budget does not belong to user");
        }

        budgetRepository.delete(budget);
        log.info("Budget deleted successfully");
    }

    @Override
    public Map<String, Double> getBudgetLimitsMap(Long userId) {
        log.debug("Getting budget limits map for user: {}", userId);

        List<Budget> budgets = budgetRepository.findCategoryBudgets(userId, BudgetPeriod.MONTHLY);

        Map<String, Double> budgetMap = new HashMap<>();
        for (Budget budget : budgets) {
            if (budget.getCategory() != null) {
                budgetMap.put(budget.getCategory().getName(), budget.getBudgetAmount().doubleValue());
            }
        }

        // Add default budgets for categories without custom budgets
        addDefaultBudgets(budgetMap);

        return budgetMap;
    }

    @Override
    public boolean isOverBudget(Long userId, String categoryName, String periodStr) {
        try {
            BudgetResponseDTO budget = getBudgetByCategory(userId, categoryName, periodStr);
            return budget.getIsOverBudget();
        } catch (Exception e) {
            return false; // No budget set, so not over budget
        }
    }

    @Override
    public BigDecimal getRemainingBudget(Long userId, String categoryName, String periodStr) {
        try {
            BudgetResponseDTO budget = getBudgetByCategory(userId, categoryName, periodStr);
            return budget.getRemaining();
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Convert Budget entity to DTO with current spending
     */
    private BudgetResponseDTO toBudgetResponseDTO(Budget budget, Long userId) {
        LocalDate[] dateRange = getDateRangeForPeriod(budget.getBudgetPeriod());
        LocalDate startDate = dateRange[0];
        LocalDate endDate = dateRange[1];

        BigDecimal currentSpending;

        if (budget.getIsTotalBudget()) {
            // Total spending across all categories
            currentSpending = expenseService.getTotalSpentInDateRange(userId, startDate, endDate);
        } else {
            // Spending for specific category
            currentSpending = expenseService.getTotalSpentByCategoryInDateRange(
                userId, budget.getCategory().getName(), startDate, endDate
            );
        }

        BigDecimal remaining = budget.getBudgetAmount().subtract(currentSpending);

        Double percentageUsed = budget.getBudgetAmount().compareTo(BigDecimal.ZERO) == 0 ? 0.0 :
            currentSpending.divide(budget.getBudgetAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();

        boolean isOverBudget = currentSpending.compareTo(budget.getBudgetAmount()) > 0;
        boolean isNearLimit = budget.getAlertThreshold() != null &&
            percentageUsed >= budget.getAlertThreshold().doubleValue();

        return BudgetResponseDTO.builder()
            .id(budget.getId())
            .categoryName(budget.getCategory() != null ? budget.getCategory().getName() : null)
            .categoryIcon(budget.getCategory() != null ? budget.getCategory().getIcon() : "💰")
            .categoryColor(budget.getCategory() != null ? budget.getCategory().getColor() : "#4ECDC4")
            .budgetAmount(budget.getBudgetAmount())
            .currentSpending(currentSpending)
            .remaining(remaining)
            .percentageUsed(percentageUsed)
            .budgetPeriod(budget.getBudgetPeriod().name())
            .alertThreshold(budget.getAlertThreshold())
            .isTotalBudget(budget.getIsTotalBudget())
            .isOverBudget(isOverBudget)
            .isNearLimit(isNearLimit)
            .createdAt(budget.getCreatedAt())
            .updatedAt(budget.getUpdatedAt())
            .build();
    }

    /**
     * Get date range for budget period
     */
    private LocalDate[] getDateRangeForPeriod(BudgetPeriod period) {
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate;

        switch (period) {
            case DAILY:
                startDate = today;
                endDate = today;
                break;
            case WEEKLY:
                startDate = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                endDate = startDate.plusDays(6);
                break;
            case MONTHLY:
                startDate = YearMonth.now().atDay(1);
                endDate = YearMonth.now().atEndOfMonth();
                break;
            case YEARLY:
                startDate = LocalDate.of(today.getYear(), 1, 1);
                endDate = LocalDate.of(today.getYear(), 12, 31);
                break;
            default:
                startDate = YearMonth.now().atDay(1);
                endDate = YearMonth.now().atEndOfMonth();
        }

        return new LocalDate[]{startDate, endDate};
    }

    /**
     * Add default budgets for categories without custom budgets
     */
    private void addDefaultBudgets(Map<String, Double> budgetMap) {
        Map<String, Double> defaults = Map.of(
            "Food", 200.0,
            "Transport", 100.0,
            "Entertainment", 150.0,
            "Bills", 300.0,
            "Shopping", 250.0,
            "Health", 200.0,
            "Education", 150.0,
            "Other", 100.0
        );

        defaults.forEach((category, amount) -> {
            if (!budgetMap.containsKey(category)) {
                budgetMap.put(category, amount);
            }
        });
    }
}

