package com.finance.concierge.controller;

import com.finance.concierge.dto.AnalyticsDTO;
import com.finance.concierge.entity.User;
import com.finance.concierge.exception.ForbiddenException;
import com.finance.concierge.service.AnalyticsService;
import com.finance.concierge.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for User-Scoped Analytics
 * Enforces strict user isolation with explicit userId path parameters
 */
@Tag(name = "User Analytics", description = "User-scoped analytics endpoints with strict isolation")
@Slf4j
@RestController
@RequestMapping("/api/users/{userId}/analytics")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class UserAnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserService userService;

    /**
     * Get analytics summary for a specific user
     */
    @GetMapping("/summary")
    @Operation(summary = "Get Analytics Summary", description = "Retrieve analytics summary for a specific user")
    public ResponseEntity<AnalyticsDTO.SummaryDTO> getSummary(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        log.info("Fetching analytics summary for userId: {}, requester: {}",
                userId, authenticatedUser.getUsername());

        // Validate authorization
        validateUserAccess(authenticatedUser, userId);

        // Validate user exists and get User object
        User targetUser = userService.getUserById(userId);

        AnalyticsDTO.SummaryDTO summary = analyticsService.getAnalyticsSummary(targetUser);

        return ResponseEntity.ok(summary);
    }

    /**
     * Get daily spending trend for a specific user
     */
    @GetMapping("/trend")
    @Operation(summary = "Get Daily Spending Trend", description = "Retrieve daily spending trend for a specific user")
    public ResponseEntity<List<AnalyticsDTO.DailyTrendDTO>> getDailyTrend(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Number of days to include in the trend")
            @RequestParam(defaultValue = "10") int days,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        log.info("Fetching daily trend for userId: {}, days: {}, requester: {}",
                userId, days, authenticatedUser.getUsername());

        // Validate authorization
        validateUserAccess(authenticatedUser, userId);

        // Validate user exists and get User object
        User targetUser = userService.getUserById(userId);

        List<AnalyticsDTO.DailyTrendDTO> trend = analyticsService.getDailyTrend(targetUser, days);

        return ResponseEntity.ok(trend);
    }

    /**
     * Get monthly spending for a specific user
     */
    @GetMapping("/monthly-spend")
    @Operation(summary = "Get Monthly Spending", description = "Retrieve monthly spending breakdown for a specific user")
    public ResponseEntity<List<AnalyticsDTO.MonthlySpendDTO>> getMonthlySpend(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Year to fetch data for (defaults to current year)")
            @RequestParam(required = false) Integer year,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }

        log.info("Fetching monthly spend for userId: {}, year: {}, requester: {}",
                userId, year, authenticatedUser.getUsername());

        // Validate authorization
        validateUserAccess(authenticatedUser, userId);

        // Validate user exists and get User object
        User targetUser = userService.getUserById(userId);

        List<AnalyticsDTO.MonthlySpendDTO> monthlySpend = analyticsService.getMonthlySpend(targetUser, year);

        return ResponseEntity.ok(monthlySpend);
    }

    /**
     * Get forecast based on historical data for a specific user
     */
    @GetMapping("/forecast")
    @Operation(summary = "Get Spending Forecast", description = "Retrieve spending forecast based on historical data")
    public ResponseEntity<AnalyticsDTO.ForecastDTO> getForecast(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        log.info("Fetching forecast for userId: {}, requester: {}",
                userId, authenticatedUser.getUsername());

        // Validate authorization
        validateUserAccess(authenticatedUser, userId);

        // Validate user exists and get User object
        User targetUser = userService.getUserById(userId);

        AnalyticsDTO.ForecastDTO forecast = analyticsService.getForecast(targetUser);

        return ResponseEntity.ok(forecast);
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
            log.debug("User {} accessing their own analytics", authenticatedUser.getUsername());
            return;
        }

        // Check if user has ADMIN role (for future admin override)
        // Currently only USER and PREMIUM roles exist, but this prepares for ADMIN role
        boolean isAdmin = authenticatedUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            log.info("Admin user {} accessing analytics for userId: {}",
                    authenticatedUser.getUsername(), targetUserId);
            return;
        }

        // User is trying to access another user's data without permission
        log.warn("Forbidden: User {} attempted to access analytics for userId: {}",
                authenticatedUser.getUsername(), targetUserId);
        throw new ForbiddenException("You can only view your own analytics");
    }
}

