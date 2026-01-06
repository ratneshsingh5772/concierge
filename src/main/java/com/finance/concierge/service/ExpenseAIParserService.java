package com.finance.concierge.service;

import com.finance.concierge.dto.ParsedExpenseDTO;

/**
 * Service interface for AI-based expense parsing
 */
public interface ExpenseAIParserService {

    /**
     * Parse user message using AI to extract expense details
     *
     * @param userMessage The natural language message from the user
     * @return ParsedExpenseDTO containing parsed expense information
     */
    ParsedExpenseDTO parseExpense(String userMessage);

    /**
     * Parse user message with historical context
     *
     * @param userMessage The natural language message from the user
     * @param userId The user ID for context
     * @return ParsedExpenseDTO containing parsed expense information
     */
    ParsedExpenseDTO parseExpenseWithContext(String userMessage, Long userId);
}

