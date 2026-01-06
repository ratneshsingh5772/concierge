package com.finance.concierge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for budget response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponseDTO {

    private Long id;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private BigDecimal budgetAmount;
    private BigDecimal currentSpending;
    private BigDecimal remaining;
    private Double percentageUsed;
    private String budgetPeriod;
    private BigDecimal alertThreshold;
    private Boolean isTotalBudget;
    private Boolean isOverBudget;
    private Boolean isNearLimit; // True if over alert threshold
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

