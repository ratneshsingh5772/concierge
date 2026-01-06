package com.finance.concierge.service.impl;

import com.finance.concierge.dto.ParsedExpenseDTO;
import com.finance.concierge.service.ExpenseAIParserService;
import com.finance.concierge.util.CategoryMappingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple regex-based implementation of ExpenseAIParserService
 * This is a fallback implementation that doesn't require AI
 */
@Slf4j
@Service
public class ExpenseAIParserServiceImpl implements ExpenseAIParserService {

    private static final Pattern AMOUNT_PATTERN = Pattern.compile("\\$?([\\d]+\\.?[\\d]*)");

    @Override
    public ParsedExpenseDTO parseExpense(String userMessage) {
        log.info("Parsing expense from message: {}", userMessage);

        if (userMessage == null || userMessage.isBlank()) {
            return ParsedExpenseDTO.failed("Message cannot be empty");
        }

        try {
            // Extract amount
            Matcher amountMatcher = AMOUNT_PATTERN.matcher(userMessage);
            BigDecimal amount = null;
            if (amountMatcher.find()) {
                amount = new BigDecimal(amountMatcher.group(1)).setScale(2, RoundingMode.HALF_UP);
            }

            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ParsedExpenseDTO.failed("Could not extract valid amount from message");
            }

            // Extract category from keywords
            String category = CategoryMappingUtil.findCategoryInMessage(userMessage);

            return ParsedExpenseDTO.success(
                    amount,
                    "USD",
                    category,
                    userMessage.trim(),
                    BigDecimal.valueOf(0.7) // Medium confidence for regex parsing
            );

        } catch (Exception e) {
            log.error("Regex parsing failed: {}", e.getMessage(), e);
            return ParsedExpenseDTO.failed("Failed to parse expense");
        }
    }

    @Override
    public ParsedExpenseDTO parseExpenseWithContext(String userMessage, Long userId) {
        // For now, just delegate to basic parse
        return parseExpense(userMessage);
    }
}

