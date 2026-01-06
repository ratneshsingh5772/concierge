package com.finance.concierge.helper;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility helper for expense parsing operations
 * Follows Single Responsibility Principle (SRP)
 */
@Slf4j
@UtilityClass
public class ExpenseParsingHelper {

    private static final Pattern CURRENCY_PATTERN = Pattern.compile("\\b([A-Z]{3})\\b");
    private static final Pattern AMOUNT_WITH_SYMBOL = Pattern.compile("(\\$|USD|€|EUR|£|GBP)?\\s*([0-9]+\\.?[0-9]*)");

    /**
     * Extract currency from message
     */
    public static String extractCurrency(String message) {
        if (message == null || message.isBlank()) {
            return "USD";
        }

        // Check for currency symbols
        if (message.contains("$") || message.toUpperCase().contains("USD")) {
            return "USD";
        }
        if (message.contains("€") || message.toUpperCase().contains("EUR")) {
            return "EUR";
        }
        if (message.contains("£") || message.toUpperCase().contains("GBP")) {
            return "GBP";
        }

        // Try to find 3-letter currency code
        Matcher matcher = CURRENCY_PATTERN.matcher(message.toUpperCase());
        if (matcher.find()) {
            String currency = matcher.group(1);
            if (isValidCurrency(currency)) {
                return currency;
            }
        }

        return "USD"; // Default
    }

    /**
     * Check if currency code is valid
     */
    private static boolean isValidCurrency(String code) {
        if (code == null || code.length() != 3) {
            return false;
        }
        // Simple validation - in production, use a comprehensive list
        return code.matches("USD|EUR|GBP|INR|JPY|CNY|AUD|CAD|CHF|NZD|SGD");
    }

    /**
     * Extract amount with better precision
     */
    public static BigDecimal extractAmount(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }

        Matcher matcher = AMOUNT_WITH_SYMBOL.matcher(message);
        if (matcher.find()) {
            try {
                String amountStr = matcher.group(2);
                return new BigDecimal(amountStr);
            } catch (NumberFormatException e) {
                log.warn("Failed to parse amount: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * Clean and normalize message
     */
    public static String normalizeMessage(String message) {
        if (message == null) {
            return "";
        }
        return message.trim()
                .replaceAll("\\s+", " ")
                .toLowerCase();
    }

    /**
     * Validate parsed expense data
     */
    public static boolean isValidExpenseData(BigDecimal amount, String category, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid amount: {}", amount);
            return false;
        }

        if (category == null || category.isBlank()) {
            log.warn("Missing category");
            return false;
        }

        if (description == null || description.isBlank()) {
            log.warn("Missing description");
            return false;
        }

        return true;
    }

    /**
     * Calculate confidence score based on parsing quality
     */
    public static BigDecimal calculateConfidence(boolean hasAmount, boolean hasCategory,
                                                 boolean hasDescription, boolean fromAI) {
        double score = 0.0;

        if (hasAmount) score += 0.4;
        if (hasCategory) score += 0.3;
        if (hasDescription) score += 0.2;
        if (fromAI) score += 0.1;

        return BigDecimal.valueOf(Math.min(score, 1.0));
    }
}

