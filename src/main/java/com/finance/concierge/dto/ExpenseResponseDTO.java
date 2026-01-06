package com.finance.concierge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for expense response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Expense response")
public class ExpenseResponseDTO {

    @Schema(description = "Expense ID", example = "1")
    private Long id;

    @Schema(description = "User ID", example = "1")
    private Long userId;

    @Schema(description = "Category ID", example = "1")
    private Long categoryId;

    @Schema(description = "Category name", example = "Food")
    private String categoryName;

    @Schema(description = "Category icon", example = "🍔")
    private String categoryIcon;

    @Schema(description = "Category color", example = "#FF6B6B")
    private String categoryColor;

    @Schema(description = "Expense amount", example = "15.50")
    private BigDecimal amount;

    @Schema(description = "Currency code", example = "USD")
    private String currency;

    @Schema(description = "Expense description", example = "Coffee at Starbucks")
    private String description;

    @Schema(description = "Original natural language message", example = "I spent $15 on coffee")
    private String originalMessage;

    @Schema(description = "Expense date", example = "2026-01-05")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expenseDate;

    @Schema(description = "Whether this expense was parsed by AI", example = "true")
    private Boolean aiParsed;

    @Schema(description = "AI confidence score (0-1)", example = "0.95")
    private BigDecimal aiConfidence;

    @Schema(description = "Creation timestamp", example = "2026-01-05T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2026-01-05T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}

