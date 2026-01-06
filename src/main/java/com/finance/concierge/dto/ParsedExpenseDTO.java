package com.finance.concierge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for parsed expense data from AI
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedExpenseDTO {

    private BigDecimal amount;

    private String currency;

    private String category;

    private String description;

    private BigDecimal confidence;

    private String rawAIResponse;

    private boolean parsingSuccessful;

    /**
     * Create a failed parsing result
     */
    public static ParsedExpenseDTO failed(String errorMessage) {
        return ParsedExpenseDTO.builder()
                .parsingSuccessful(false)
                .description(errorMessage)
                .build();
    }

    /**
     * Create a successful parsing result
     */
    public static ParsedExpenseDTO success(BigDecimal amount, String currency, String category,
                                          String description, BigDecimal confidence) {
        return ParsedExpenseDTO.builder()
                .amount(amount)
                .currency(currency != null ? currency : "USD")
                .category(category)
                .description(description)
                .confidence(confidence)
                .parsingSuccessful(true)
                .build();
    }
}

