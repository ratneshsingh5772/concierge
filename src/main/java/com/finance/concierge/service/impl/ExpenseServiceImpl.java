package com.finance.concierge.service.impl;

import com.finance.concierge.dto.ParsedExpenseDTO;
import com.finance.concierge.entity.Category;
import com.finance.concierge.entity.Expense;
import com.finance.concierge.entity.User;
import com.finance.concierge.exception.ResourceNotFoundException;
import com.finance.concierge.repository.CategoryRepository;
import com.finance.concierge.repository.ExpenseRepository;
import com.finance.concierge.repository.UserRepository;
import com.finance.concierge.service.ExpenseAIParserService;
import com.finance.concierge.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Implementation of ExpenseService
 * Follows SOLID principles
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseAIParserService aiParserService;

    @Override
    @Transactional
    public Expense createExpense(Long userId, ParsedExpenseDTO parsedData) {
        log.info("Creating expense for user: {} with data: {}", userId, parsedData);

        if (!parsedData.isParsingSuccessful()) {
            throw new IllegalArgumentException("Cannot create expense from failed parsing: " + parsedData.getDescription());
        }

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Find or validate category
        Category category = categoryRepository.findByNameIgnoreCase(parsedData.getCategory())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + parsedData.getCategory()));

        if (!category.getIsActive()) {
            throw new IllegalArgumentException("Category is not active: " + parsedData.getCategory());
        }

        // Create expense
        Expense expense = Expense.builder()
                .user(user)
                .category(category)
                .amount(parsedData.getAmount())
                .currency(parsedData.getCurrency() != null ? parsedData.getCurrency() : "USD")
                .description(parsedData.getDescription())
                .expenseDate(LocalDate.now())
                .aiParsed(true)
                .aiConfidence(parsedData.getConfidence())
                .build();

        Expense saved = expenseRepository.save(expense);
        log.info("Expense created successfully with ID: {}", saved.getId());

        return saved;
    }

    @Override
    @Transactional
    public Expense createExpenseFromMessage(String message, Long userId) {
        log.info("Creating expense from message for user {}: {}", userId, message);

        // Parse the message using AI
        ParsedExpenseDTO parsed = aiParserService.parseExpense(message);

        if (!parsed.isParsingSuccessful()) {
            log.warn("Failed to parse expense message: {}", message);
            throw new IllegalArgumentException("Could not parse expense from message: " + parsed.getDescription());
        }

        // Store original message
        Expense expense = createExpense(userId, parsed);
        expense.setOriginalMessage(message);

        return expenseRepository.save(expense);
    }

    @Override
    public List<Expense> getExpensesByCategory(Long userId, String categoryName) {
        log.debug("Getting expenses for user {} and category {}", userId, categoryName);
        return expenseRepository.findByUserIdAndCategoryName(userId, categoryName);
    }

    @Override
    public List<Expense> getExpensesByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        log.debug("Getting expenses for user {} between {} and {}", userId, startDate, endDate);
        return expenseRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }

    @Override
    public BigDecimal getTotalSpentByCategory(Long userId, String categoryName) {
        log.debug("Calculating total spent for user {} in category {}", userId, categoryName);
        BigDecimal total = expenseRepository.sumAmountByUserIdAndCategoryName(userId, categoryName);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalSpentInDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        log.debug("Calculating total spent for user {} between {} and {}", userId, startDate, endDate);
        BigDecimal total = expenseRepository.sumAmountByUserIdAndDateRange(userId, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalSpentByCategoryInDateRange(Long userId, String categoryName,
                                                         LocalDate startDate, LocalDate endDate) {
        log.debug("Calculating total spent for user {} in category {} between {} and {}",
                userId, categoryName, startDate, endDate);
        BigDecimal total = expenseRepository.sumAmountByUserIdCategoryAndDateRange(
                userId, categoryName, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public List<Expense> getCurrentMonthExpenses(Long userId) {
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        log.debug("Getting current month expenses for user {}", userId);
        return getExpensesByDateRange(userId, startDate, endDate);
    }

    @Override
    public BigDecimal getTotalSpentThisMonth(Long userId) {
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        return getTotalSpentInDateRange(userId, startDate, endDate);
    }
}

