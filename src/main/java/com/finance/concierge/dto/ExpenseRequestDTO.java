package com.finance.concierge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating an expense
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create an expense")
public class ExpenseRequestDTO {

    @Schema(description = "User ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "User ID is required")
    private Long userId;

    @Schema(description = "Category ID", example = "1")
    private Long categoryId;

    @Schema(description = "Category name (alternative to categoryId)", example = "Food")
    private String categoryName;

    @Schema(description = "Expense amount", example = "15.50", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @Schema(description = "Currency code", example = "USD")
    @Builder.Default
    private String currency = "USD";

    @Schema(description = "Expense description", example = "Coffee at Starbucks")
    private String description;

    @Schema(description = "Expense date (defaults to today)", example = "2026-01-05")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expenseDate;

    @Schema(description = "Original natural language message", example = "I spent $15 on coffee")
    private String originalMessage;

    @Schema(description = "Whether this expense was parsed by AI", example = "true")
    @Builder.Default
    private Boolean aiParsed = false;

    @Schema(description = "AI confidence score (0-1)", example = "0.95")
    private BigDecimal aiConfidence;
}

