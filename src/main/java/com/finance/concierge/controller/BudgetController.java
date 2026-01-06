package com.finance.concierge.controller;

import com.finance.concierge.common.ApiResponse;
import com.finance.concierge.dto.BudgetRequestDTO;
import com.finance.concierge.dto.BudgetResponseDTO;
import com.finance.concierge.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Budget Management
 * Allows users to set and manage category-wise and total budgets
 */
@Slf4j
@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Tag(name = "Budget Management", description = "APIs for setting and managing budgets")
@SecurityRequirement(name = "Bearer Authentication")
public class BudgetController {

    private final BudgetService budgetService;

    /**
     * Set or update budget for a category
     */
    @PostMapping("/category")
    @Operation(summary = "Set Category Budget", description = "Create or update budget for a specific category")
    public ResponseEntity<ApiResponse<BudgetResponseDTO>> setCategoryBudget(
        @Valid @RequestBody BudgetRequestDTO request,
        Authentication authentication
    ) {
        log.info("Setting category budget: {} for user: {}", request.getCategoryName(), authentication.getName());

        Long userId = getUserIdFromAuth(authentication);
        BudgetResponseDTO response = budgetService.setBudget(userId, request);

        return ResponseEntity.ok(ApiResponse.success(response,
            "Budget set successfully for " + request.getCategoryName()));
    }

    /**
     * Set or update total budget
     */
    @PostMapping("/total")
    @Operation(summary = "Set Total Budget", description = "Create or update total monthly budget")
    public ResponseEntity<ApiResponse<BudgetResponseDTO>> setTotalBudget(
        @Valid @RequestBody BudgetRequestDTO request,
        Authentication authentication
    ) {
        log.info("Setting total budget for user: {}", authentication.getName());

        Long userId = getUserIdFromAuth(authentication);
        request.setIsTotalBudget(true);
        BudgetResponseDTO response = budgetService.setTotalBudget(userId, request);

        return ResponseEntity.ok(ApiResponse.success(response, "Total budget set successfully"));
    }

    /**
     * Get all budgets for user
     */
    @GetMapping
    @Operation(summary = "Get All Budgets", description = "Retrieve all budgets (category-wise and total)")
    public ResponseEntity<ApiResponse<List<BudgetResponseDTO>>> getAllBudgets(
        @Parameter(description = "Budget period (DAILY, WEEKLY, MONTHLY, YEARLY)")
        @RequestParam(defaultValue = "MONTHLY") String period,
        Authentication authentication
    ) {
        log.info("Fetching all budgets for user: {}", authentication.getName());

        Long userId = getUserIdFromAuth(authentication);
        List<BudgetResponseDTO> budgets = budgetService.getAllBudgets(userId, period);

        return ResponseEntity.ok(ApiResponse.success(budgets,
            "Retrieved " + budgets.size() + " budget(s)"));
    }

    /**
     * Get budget for specific category
     */
    @GetMapping("/category/{categoryName}")
    @Operation(summary = "Get Category Budget", description = "Retrieve budget for a specific category")
    public ResponseEntity<ApiResponse<BudgetResponseDTO>> getCategoryBudget(
        @PathVariable String categoryName,
        @RequestParam(defaultValue = "MONTHLY") String period,
        Authentication authentication
    ) {
        log.info("Fetching budget for category: {} for user: {}", categoryName, authentication.getName());

        Long userId = getUserIdFromAuth(authentication);
        BudgetResponseDTO budget = budgetService.getBudgetByCategory(userId, categoryName, period);

        return ResponseEntity.ok(ApiResponse.success(budget,
            "Budget for " + categoryName + " retrieved successfully"));
    }

    /**
     * Get total budget
     */
    @GetMapping("/total")
    @Operation(summary = "Get Total Budget", description = "Retrieve total budget")
    public ResponseEntity<ApiResponse<BudgetResponseDTO>> getTotalBudget(
        @RequestParam(defaultValue = "MONTHLY") String period,
        Authentication authentication
    ) {
        log.info("Fetching total budget for user: {}", authentication.getName());

        Long userId = getUserIdFromAuth(authentication);
        BudgetResponseDTO budget = budgetService.getTotalBudget(userId, period);

        return ResponseEntity.ok(ApiResponse.success(budget, "Total budget retrieved successfully"));
    }

    /**
     * Delete budget
     */
    @DeleteMapping("/{budgetId}")
    @Operation(summary = "Delete Budget", description = "Delete a budget by ID")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(
        @PathVariable Long budgetId,
        Authentication authentication
    ) {
        log.info("Deleting budget: {} for user: {}", budgetId, authentication.getName());

        Long userId = getUserIdFromAuth(authentication);
        budgetService.deleteBudget(userId, budgetId);

        return ResponseEntity.ok(ApiResponse.success(null, "Budget deleted successfully"));
    }

    /**
     * Get budget limits as Map (for AI agent)
     */
    @GetMapping("/limits")
    @Operation(summary = "Get Budget Limits Map", description = "Get all budget limits as a map (for AI)")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getBudgetLimits(
        Authentication authentication
    ) {
        log.info("Fetching budget limits map for user: {}", authentication.getName());

        Long userId = getUserIdFromAuth(authentication);
        Map<String, Double> limits = budgetService.getBudgetLimitsMap(userId);

        return ResponseEntity.ok(ApiResponse.success(limits, "Budget limits retrieved successfully"));
    }

    /**
     * Batch set budgets (multiple categories at once)
     */
    @PostMapping("/batch")
    @Operation(summary = "Batch Set Budgets", description = "Set multiple category budgets at once")
    public ResponseEntity<ApiResponse<List<BudgetResponseDTO>>> batchSetBudgets(
        @RequestBody List<BudgetRequestDTO> requests,
        Authentication authentication
    ) {
        log.info("Batch setting {} budgets for user: {}", requests.size(), authentication.getName());

        Long userId = getUserIdFromAuth(authentication);
        List<BudgetResponseDTO> responses = requests.stream()
            .map(request -> budgetService.setBudget(userId, request))
            .toList();

        return ResponseEntity.ok(ApiResponse.success(responses,
            "Successfully set " + responses.size() + " budgets"));
    }

    /**
     * Helper: Extract user ID from authentication
     */
    private Long getUserIdFromAuth(Authentication authentication) {
        // TODO: Get actual user ID from authentication/UserDetailsService
        return 1L;
    }
}

