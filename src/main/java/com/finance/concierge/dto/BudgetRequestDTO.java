package com.finance.concierge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO for setting budget
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to set budget for a category or total budget")
public class BudgetRequestDTO {

    @Schema(description = "Category name (null for total budget)", example = "Food")
    private String categoryName;

    @NotNull(message = "Budget amount is required")
    @DecimalMin(value = "0.01", message = "Budget amount must be greater than 0")
    @Schema(description = "Budget amount", example = "200.00")
    private BigDecimal budgetAmount;

    @Schema(description = "Budget period (DAILY, WEEKLY, MONTHLY, YEARLY)", example = "MONTHLY", defaultValue = "MONTHLY")
    private String budgetPeriod = "MONTHLY";

    @Schema(description = "Alert threshold percentage (e.g., 80 for 80%)", example = "80.0")
    private BigDecimal alertThreshold;

    @Schema(description = "Set as total budget (ignores categoryName)", example = "false")
    private Boolean isTotalBudget = false;
}
