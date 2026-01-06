package com.finance.concierge.service.impl;

import com.finance.concierge.dto.ParsedExpenseDTO;
import com.finance.concierge.entity.Expense;
import com.finance.concierge.repository.ExpenseRepository;
import com.finance.concierge.service.BudgetService;
import com.finance.concierge.service.ExpenseService;
import com.finance.concierge.service.FinanceAgentToolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVWriter;

/**
 * Implementation of FinanceAgentToolService
 * Handles database persistence for finance agent operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinanceAgentToolServiceImpl implements FinanceAgentToolService {

    private final ExpenseService expenseService;
    private final ExpenseRepository expenseRepository;
    private final BudgetService budgetService;

    private static final String CSV_FILE = "expenses.csv";
    private static final Map<String, Double> BUDGETS = Map.of(
        "Food", 200.0,
        "Transport", 100.0,
        "Entertainment", 150.0,
        "Bills", 300.0,
        "Shopping", 250.0,
        "Health", 200.0,
        "Education", 150.0,
        "Other", 100.0
    );

    @Override
    @Transactional
    public Map<String, String> logExpense(double amount, String category, String description, Long userId) {
        log.info("Logging expense: ${} for {} - {} (User: {})", amount, category, description, userId);

        try {
            // 1. Save to database using ExpenseService
            ParsedExpenseDTO parsedExpense = ParsedExpenseDTO.success(
                BigDecimal.valueOf(amount),
                "USD",
                category,
                description,
                BigDecimal.valueOf(1.0) // High confidence since it's from agent parsing
            );

            Expense expense = expenseService.createExpense(userId, parsedExpense);
            expense.setOriginalMessage(description);
            log.info("Expense saved to database with ID: {}", expense.getId());

            // 2. Also save to CSV for backward compatibility (optional)
            saveToCsv(amount, category, description);

            return Map.of("result", String.format("Logged $%.2f to %s", amount, category));

        } catch (Exception e) {
            log.error("Error logging expense to database: {}", e.getMessage(), e);
            return Map.of("error", "Error logging expense: " + e.getMessage());
        }
    }

    @Override
    public Map<String, String> getBudgetStatus(String category, Long userId) {
        log.debug("Getting budget status for category: {} (User: {})", category, userId);

        try {
            // Try to get user's custom budget first
            Map<String, Double> budgetLimits = budgetService.getBudgetLimitsMap(userId);

            // Normalize category name
            String normalizedCategory = budgetLimits.keySet().stream()
                .filter(k -> k.equalsIgnoreCase(category))
                .findFirst()
                .orElse(category);

            if (!budgetLimits.containsKey(normalizedCategory)) {
                return Map.of("error", "No budget defined for category: " + category +
                    ". Available categories: Food, Transport, Entertainment, Bills, Shopping, Health, Education, Other");
            }

            double budget = budgetLimits.get(normalizedCategory);

            // Calculate total spent from database
            LocalDate startOfMonth = YearMonth.now().atDay(1);
            LocalDate endOfMonth = YearMonth.now().atEndOfMonth();

            BigDecimal totalSpent = expenseService.getTotalSpentByCategoryInDateRange(
                userId, normalizedCategory, startOfMonth, endOfMonth
            );

            double spent = totalSpent.doubleValue();
            double remaining = budget - spent;

            String result = String.format(
                "You have spent $%.2f out of $%.2f on %s. Remaining: $%.2f.",
                spent, budget, normalizedCategory, remaining
            );

            return Map.of("result", result);

        } catch (Exception e) {
            log.error("Error getting budget status: {}", e.getMessage(), e);
            return Map.of("error", "Error getting budget status: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> createMonthlyReport(Long userId) {
        log.info("Creating monthly report for user: {}", userId);

        LocalDate startOfMonth = YearMonth.now().atDay(1);
        LocalDate endOfMonth = YearMonth.now().atEndOfMonth();

        List<Expense> monthlyExpenses = expenseRepository.findByUserIdAndDateBetween(
            userId, startOfMonth, endOfMonth
        );

        Map<String, Double> categoryTotals = new HashMap<>();
        double grandTotal = 0.0;

        for (Expense expense : monthlyExpenses) {
            String category = expense.getCategory().getName();
            double amount = expense.getAmount().doubleValue();

            categoryTotals.merge(category, amount, Double::sum);
            grandTotal += amount;
        }

        Map<String, Object> report = new HashMap<>();
        report.put("month", YearMonth.now().toString());
        report.put("categoryTotals", categoryTotals);
        report.put("grandTotal", grandTotal);
        report.put("transactionCount", monthlyExpenses.size());

        return report;
    }

    /**
     * Save to CSV for backward compatibility
     */
    private void saveToCsv(double amount, String category, String description) {
        try {
            File file = new File(CSV_FILE);
            boolean fileExists = file.exists();

            try (CSVWriter writer = new CSVWriter(new FileWriter(file, true))) {
                if (!fileExists) {
                    writer.writeNext(new String[]{"Date", "Category", "Amount", "Description"});
                }
                writer.writeNext(new String[]{
                    LocalDate.now().toString(),
                    category,
                    String.valueOf(amount),
                    description
                });
            }
            log.debug("Expense also saved to CSV file");
        } catch (IOException e) {
            log.warn("Could not save to CSV file: {}", e.getMessage());
            // Don't fail the whole operation if CSV write fails
        }
    }
}

