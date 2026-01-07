package com.finance.concierge.controller;

import com.finance.concierge.common.ApiResponse;
import com.finance.concierge.dto.DashboardStatsDTO;
import com.finance.concierge.dto.ExpenseResponseDTO;
import com.finance.concierge.entity.Expense;
import com.finance.concierge.entity.User;
import com.finance.concierge.service.DashboardService;
import com.finance.concierge.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for expense management and analytics
 * Provides endpoints for data visualization and reporting
 */
@Slf4j
@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses & Analytics", description = "Expense tracking and data visualization APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final DashboardService dashboardService;

    /**
     * Get dashboard statistics for current month
     *
     * @return Dashboard statistics including charts data
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Get Dashboard Statistics", description = "Returns comprehensive dashboard data for visualization")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getDashboard(Authentication authentication) {
        log.info("Fetching dashboard stats for user: {}", authentication.getName());

        Long userId = getUserIdFromAuth(authentication);
        DashboardStatsDTO stats = dashboardService.getDashboardStats(userId);

        return ResponseEntity.ok(ApiResponse.success(stats, "Dashboard statistics retrieved successfully"));
    }

    /**
     * Get dashboard statistics for custom date range
     */
    @GetMapping("/dashboard/range")
    @Operation(summary = "Get Dashboard for Date Range", description = "Returns dashboard data for a specific date range")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getDashboardForDateRange(
        @Parameter(description = "Start date (YYYY-MM-DD)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @Parameter(description = "End date (YYYY-MM-DD)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        Authentication authentication
    ) {
        log.info("Fetching dashboard stats for user: {} from {} to {}",
            authentication.getName(), startDate, endDate);

        Long userId = getUserIdFromAuth(authentication);
        DashboardStatsDTO stats = dashboardService.getDashboardStatsForDateRange(userId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(stats, "Dashboard statistics retrieved successfully"));
    }

    /**
     * Get all expenses for current month
     */
    @GetMapping("/current-month")
    @Operation(summary = "Get Current Month Expenses", description = "Returns all expenses for the current month")
    public ResponseEntity<ApiResponse<List<ExpenseResponseDTO>>> getCurrentMonthExpenses(Authentication authentication) {
        log.info("Fetching current month expenses for user: {}", authentication.getName());

        Long userId = getUserIdFromAuth(authentication);
        List<Expense> expenses = expenseService.getCurrentMonthExpenses(userId);

        List<ExpenseResponseDTO> response = expenses.stream()
            .map(this::toExpenseResponseDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response,
            "Retrieved " + response.size() + " expenses for current month"));
    }

    /**
     * Get expenses by category
     */
    @GetMapping("/category/{categoryName}")
    @Operation(summary = "Get Expenses by Category", description = "Returns all expenses for a specific category")
    public ResponseEntity<ApiResponse<List<ExpenseResponseDTO>>> getExpensesByCategory(
        @Parameter(description = "Category name (Food, Transport, Bills, etc.)")
        @PathVariable String categoryName,
        Authentication authentication
    ) {
        log.info("Fetching {} expenses for user: {}", categoryName, authentication.getName());

        Long userId = getUserIdFromAuth(authentication);
        List<Expense> expenses = expenseService.getExpensesByCategory(userId, categoryName);

        List<ExpenseResponseDTO> response = expenses.stream()
            .map(this::toExpenseResponseDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response,
            "Retrieved " + response.size() + " " + categoryName + " expenses"));
    }

    /**
     * Get expenses for date range
     */
    @GetMapping("/range")
    @Operation(summary = "Get Expenses by Date Range", description = "Returns expenses for a specific date range")
    public ResponseEntity<ApiResponse<List<ExpenseResponseDTO>>> getExpensesByDateRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        Authentication authentication
    ) {
        log.info("Fetching expenses for user: {} from {} to {}",
            authentication.getName(), startDate, endDate);

        Long userId = getUserIdFromAuth(authentication);
        List<Expense> expenses = expenseService.getExpensesByDateRange(userId, startDate, endDate);

        List<ExpenseResponseDTO> response = expenses.stream()
            .map(this::toExpenseResponseDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response,
            "Retrieved " + response.size() + " expenses"));
    }

    /**
     * Get total spent this month
     */
    @GetMapping("/total/current-month")
    @Operation(summary = "Get Total Spent This Month", description = "Returns the total amount spent in current month")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalSpentThisMonth(Authentication authentication) {
        log.info("Fetching total spent this month for user: {}", authentication.getName());

        Long userId = getUserIdFromAuth(authentication);
        BigDecimal total = expenseService.getTotalSpentThisMonth(userId);

        return ResponseEntity.ok(ApiResponse.success(total,
            "Total spent this month: $" + total));
    }

    /**
     * Get total spent by category
     */
    @GetMapping("/total/category/{categoryName}")
    @Operation(summary = "Get Total by Category", description = "Returns total amount spent in a category (all time)")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalByCategory(
        @PathVariable String categoryName,
        Authentication authentication
    ) {
        log.info("Fetching total for category {} for user: {}", categoryName, authentication.getName());

        Long userId = getUserIdFromAuth(authentication);
        BigDecimal total = expenseService.getTotalSpentByCategory(userId, categoryName);

        return ResponseEntity.ok(ApiResponse.success(total,
            "Total spent on " + categoryName + ": $" + total));
    }

    /**
     * Get total spent by category for current month
     */
    @GetMapping("/total/category/{categoryName}/current-month")
    @Operation(summary = "Get Total by Category (Current Month)",
               description = "Returns total amount spent in a category for current month")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalByCategoryCurrentMonth(
        @PathVariable String categoryName,
        Authentication authentication
    ) {
        log.info("Fetching total for category {} (current month) for user: {}",
            categoryName, authentication.getName());

        Long userId = getUserIdFromAuth(authentication);
        LocalDate startOfMonth = YearMonth.now().atDay(1);
        LocalDate endOfMonth = YearMonth.now().atEndOfMonth();

        BigDecimal total = expenseService.getTotalSpentByCategoryInDateRange(
            userId, categoryName, startOfMonth, endOfMonth);

        return ResponseEntity.ok(ApiResponse.success(total,
            "Total spent on " + categoryName + " this month: $" + total));
    }

    /**
     * Get category breakdown (for pie chart)
     */
    @GetMapping("/breakdown/category")
    @Operation(summary = "Get Category Breakdown", description = "Returns spending breakdown by category (pie chart data)")
    public ResponseEntity<ApiResponse<List<DashboardStatsDTO.CategorySummary>>> getCategoryBreakdown(
        Authentication authentication
    ) {
        log.info("Fetching category breakdown for user: {}", authentication.getName());

        Long userId = getUserIdFromAuth(authentication);
        DashboardStatsDTO stats = dashboardService.getDashboardStats(userId);

        return ResponseEntity.ok(ApiResponse.success(stats.getCategoryBreakdown(),
            "Category breakdown retrieved successfully"));
    }

    /**
     * Get daily spending (for line chart)
     */
    @GetMapping("/trends/daily")
    @Operation(summary = "Get Daily Spending Trend", description = "Returns daily spending for last 30 days (line chart data)")
    public ResponseEntity<ApiResponse<List<DashboardStatsDTO.DailySpending>>> getDailyTrend(
        Authentication authentication
    ) {
        log.info("Fetching daily spending trend for user: {}", authentication.getName());

        Long userId = getUserIdFromAuth(authentication);
        DashboardStatsDTO stats = dashboardService.getDashboardStats(userId);

        return ResponseEntity.ok(ApiResponse.success(stats.getDailySpending(),
            "Daily spending trend retrieved successfully"));
    }

    /**
     * Get budget status (for progress bars)
     */
    @GetMapping("/budget/status")
    @Operation(summary = "Get Budget Status", description = "Returns budget usage for all categories (progress bar data)")
    public ResponseEntity<ApiResponse<List<DashboardStatsDTO.BudgetStatus>>> getBudgetStatus(
        Authentication authentication
    ) {
        log.info("Fetching budget status for user: {}", authentication.getName());

        Long userId = getUserIdFromAuth(authentication);
        DashboardStatsDTO stats = dashboardService.getDashboardStats(userId);

        return ResponseEntity.ok(ApiResponse.success(stats.getBudgetStatus(),
            "Budget status retrieved successfully"));
    }

    /**
     * Helper: Extract user ID from authentication
     */
    private Long getUserIdFromAuth(Authentication authentication) {
        // The principal is the User entity (implements UserDetails)
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }

    /**
     * Helper: Convert Expense entity to DTO
     */
    private ExpenseResponseDTO toExpenseResponseDTO(Expense expense) {
        return ExpenseResponseDTO.builder()
            .id(expense.getId())
            .amount(expense.getAmount())
            .currency(expense.getCurrency())
            .categoryName(expense.getCategory().getName())
            .categoryIcon(expense.getCategory().getIcon())
            .categoryColor(expense.getCategory().getColor())
            .description(expense.getDescription())
            .expenseDate(expense.getExpenseDate())
            .aiParsed(expense.getAiParsed())
            .originalMessage(expense.getOriginalMessage())
            .createdAt(expense.getCreatedAt())
            .build();
    }
}

