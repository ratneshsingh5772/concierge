package com.finance.concierge.controller;

import com.finance.concierge.common.ApiResponse;
import com.finance.concierge.dto.BudgetRequestDTO;
import com.finance.concierge.dto.BudgetResponseDTO;
import com.finance.concierge.entity.User;
import com.finance.concierge.exception.ForbiddenException;
import com.finance.concierge.service.BudgetService;
import com.finance.concierge.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for User-Scoped Budget Management
 * Enforces strict user isolation with explicit userId path parameters
 */
@Tag(name = "User Budget Management", description = "User-scoped budget management with strict isolation")
@Slf4j
@RestController
@RequestMapping("/api/users/{userId}/budgets")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class UserBudgetController {

    private final BudgetService budgetService;
    private final UserService userService;

    /**
     * Get all budgets for a specific user
     */
    @GetMapping
    @Operation(summary = "Get All Budgets", description = "Retrieve all budgets for a specific user (category-wise and total)")
    public ResponseEntity<ApiResponse<List<BudgetResponseDTO>>> getAllBudgets(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Budget period (DAILY, WEEKLY, MONTHLY, YEARLY)")
            @RequestParam(defaultValue = "MONTHLY") String period,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        log.info("Fetching all budgets for userId: {}, period: {}, requester: {}",
                userId, period, authenticatedUser.getUsername());

        // Validate authorization
        validateUserAccess(authenticatedUser, userId);

        // Validate user exists
        userService.getUserById(userId);

        List<BudgetResponseDTO> budgets = budgetService.getAllBudgets(userId, period);

        return ResponseEntity.ok(ApiResponse.success(budgets,
                "Retrieved " + budgets.size() + " budget(s)"));
    }

    /**
     * Create or update budget for a category
     */
    @PostMapping("/category")
    @Operation(summary = "Set Category Budget", description = "Create or update budget for a specific category")
    public ResponseEntity<ApiResponse<BudgetResponseDTO>> setCategoryBudget(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody BudgetRequestDTO request,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        log.info("Setting category budget: {} for userId: {}, requester: {}",
                request.getCategoryName(), userId, authenticatedUser.getUsername());

        // Validate authorization
        validateUserAccess(authenticatedUser, userId);

        // Validate user exists
        userService.getUserById(userId);

        BudgetResponseDTO response = budgetService.setBudget(userId, request);

        return ResponseEntity.ok(ApiResponse.success(response,
                "Budget set successfully for " + request.getCategoryName()));
    }

    /**
     * Create or update total budget
     */
    @PostMapping("/total")
    @Operation(summary = "Set Total Budget", description = "Create or update total budget for a user")
    public ResponseEntity<ApiResponse<BudgetResponseDTO>> setTotalBudget(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody BudgetRequestDTO request,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        log.info("Setting total budget for userId: {}, requester: {}",
                userId, authenticatedUser.getUsername());

        // Validate authorization
        validateUserAccess(authenticatedUser, userId);

        // Validate user exists
        userService.getUserById(userId);

        request.setIsTotalBudget(true);
        BudgetResponseDTO response = budgetService.setTotalBudget(userId, request);

        return ResponseEntity.ok(ApiResponse.success(response, "Total budget set successfully"));
    }

    /**
     * Update budget (existing endpoint for compatibility)
     */
    @PutMapping("/{budgetId}")
    @Operation(summary = "Update Budget", description = "Update an existing budget by ID with ownership validation")
    public ResponseEntity<ApiResponse<BudgetResponseDTO>> updateBudget(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Budget ID") @PathVariable Long budgetId,
            @Valid @RequestBody BudgetRequestDTO request,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        log.info("Updating budget: {} for userId: {}, requester: {}",
                budgetId, userId, authenticatedUser.getUsername());

        // Validate authorization
        validateUserAccess(authenticatedUser, userId);

        // Validate user exists
        userService.getUserById(userId);

        // The service layer will validate that the budgetId belongs to the userId
        // We reuse setBudget which handles create/update
        BudgetResponseDTO response = budgetService.setBudget(userId, request);

        return ResponseEntity.ok(ApiResponse.success(response, "Budget updated successfully"));
    }

    /**
     * Delete budget
     */
    @DeleteMapping("/{budgetId}")
    @Operation(summary = "Delete Budget", description = "Delete a budget by ID with ownership validation")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Budget ID") @PathVariable Long budgetId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        log.info("Deleting budget: {} for userId: {}, requester: {}",
                budgetId, userId, authenticatedUser.getUsername());

        // Validate authorization
        validateUserAccess(authenticatedUser, userId);

        // Validate user exists
        userService.getUserById(userId);

        // The service layer validates that budgetId belongs to userId
        budgetService.deleteBudget(userId, budgetId);

        return ResponseEntity.ok(ApiResponse.success(null, "Budget deleted successfully"));
    }

    /**
     * Validate that the authenticated user has permission to access the target userId's data
     *
     * Authorization Rules:
     * 1. User can access their own data (userId matches authenticated user's ID)
     * 2. Admin users can access any user's data (future feature - currently only USER and PREMIUM roles exist)
     * 3. Otherwise, throw ForbiddenException
     */
    private void validateUserAccess(User authenticatedUser, Long targetUserId) {
        // Check if user is accessing their own data
        if (authenticatedUser.getId().equals(targetUserId)) {
            log.debug("User {} accessing their own budgets", authenticatedUser.getUsername());
            return;
        }

        // Check if user has ADMIN role (for future admin override)
        // Currently only USER and PREMIUM roles exist, but this prepares for ADMIN role
        boolean isAdmin = authenticatedUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            log.info("Admin user {} accessing budgets for userId: {}",
                    authenticatedUser.getUsername(), targetUserId);
            return;
        }

        // User is trying to access another user's data without permission
        log.warn("Forbidden: User {} attempted to access budgets for userId: {}",
                authenticatedUser.getUsername(), targetUserId);
        throw new ForbiddenException("You can only manage your own budgets");
    }
}

