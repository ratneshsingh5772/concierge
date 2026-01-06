package com.finance.concierge.service.impl;

import com.finance.concierge.dto.AnalyticsDTO;
import com.finance.concierge.entity.User;
import com.finance.concierge.repository.ExpenseRepository;
import com.finance.concierge.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ExpenseRepository expenseRepository;

    @Override
    public List<AnalyticsDTO.DailyTrendDTO> getDailyTrend(User user, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1); // Include today

        List<Object[]> results = expenseRepository.findDailySpending(user, startDate, endDate);

        // Convert to Map for easier lookup
        Map<LocalDate, BigDecimal> spendingMap = results.stream()
                .collect(Collectors.toMap(
                        obj -> (LocalDate) obj[0],
                        obj -> (BigDecimal) obj[1]
                ));

        List<AnalyticsDTO.DailyTrendDTO> trend = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            BigDecimal amount = spendingMap.getOrDefault(date, BigDecimal.ZERO);

            trend.add(AnalyticsDTO.DailyTrendDTO.builder()
                    .date(date.toString())
                    .day(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .amount(amount)
                    .build());
        }

        return trend;
    }

    @Override
    public List<AnalyticsDTO.MonthlySpendDTO> getMonthlySpend(User user, int year) {
        List<Object[]> results = expenseRepository.findMonthlySpending(user, year);

        // Initialize all months with 0
        Map<Integer, BigDecimal> monthlyMap = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            monthlyMap.put(i, BigDecimal.ZERO);
        }

        // Fill with actual data
        for (Object[] result : results) {
            monthlyMap.put((Integer) result[0], (BigDecimal) result[1]);
        }

        List<AnalyticsDTO.MonthlySpendDTO> monthlySpends = new ArrayList<>();
        String[] monthNames = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        for (int i = 1; i <= 12; i++) {
            monthlySpends.add(AnalyticsDTO.MonthlySpendDTO.builder()
                    .month(monthNames[i])
                    .amount(monthlyMap.get(i))
                    .build());
        }

        return monthlySpends;
    }

    @Override
    public AnalyticsDTO.SummaryDTO getAnalyticsSummary(User user) {
        LocalDate today = LocalDate.now();

        // 1. Total Spent Last 10 Days
        LocalDate last10DaysStart = today.minusDays(9);
        BigDecimal last10DaysTotal = expenseRepository.findTotalSpentInPeriod(user, last10DaysStart, today);
        if (last10DaysTotal == null) last10DaysTotal = BigDecimal.ZERO;

        // 2. Projected Monthly Spend
        LocalDate startOfMonth = today.withDayOfMonth(1);

        BigDecimal currentMonthTotal = expenseRepository.findTotalSpentInPeriod(user, startOfMonth, today);
        if (currentMonthTotal == null) currentMonthTotal = BigDecimal.ZERO;

        BigDecimal projectedSpend = BigDecimal.ZERO;
        int daysPassed = today.getDayOfMonth();
        int totalDaysInMonth = today.lengthOfMonth();

        if (daysPassed > 0) {
            // formula: (total / daysPassed) * totalDays
            projectedSpend = currentMonthTotal.divide(BigDecimal.valueOf(daysPassed), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(totalDaysInMonth));
        }

        // 3. Highest Daily Spend (All time or logical default to query)
        // Using all time based on repo method prompt
        List<Object[]> highest = expenseRepository.findHighestDailySpends(user, PageRequest.of(0, 1));

        AnalyticsDTO.HighestDailySpendDTO highestDTO = null;
        if (!highest.isEmpty()) {
            Object[] row = highest.get(0);
            highestDTO = AnalyticsDTO.HighestDailySpendDTO.builder()
                    .date(((LocalDate) row[0]).toString())
                    .amount((BigDecimal) row[1])
                    .build();
        } else {
             highestDTO = AnalyticsDTO.HighestDailySpendDTO.builder()
                    .date(today.toString())
                    .amount(BigDecimal.ZERO)
                    .build();
        }

        return AnalyticsDTO.SummaryDTO.builder()
                .totalSpentLast10Days(last10DaysTotal)
                .projectedMonthlySpend(projectedSpend)
                .highestDailySpend(highestDTO)
                .build();
    }

    @Override
    public AnalyticsDTO.ForecastDTO getForecast(User user) {
        LocalDate today = LocalDate.now();
        int year = today.getYear();

        // 1. Month End Forecast
        LocalDate startOfMonth = today.withDayOfMonth(1);
        BigDecimal currentMonthTotal = expenseRepository.findTotalSpentInPeriod(user, startOfMonth, today);
        if (currentMonthTotal == null) currentMonthTotal = BigDecimal.ZERO;

        BigDecimal predictedMonthEnd = BigDecimal.ZERO;
        int daysPassed = today.getDayOfMonth();
        int totalDaysInMonth = today.lengthOfMonth();

        if (daysPassed > 0) {
            predictedMonthEnd = currentMonthTotal.divide(BigDecimal.valueOf(daysPassed), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(totalDaysInMonth));
        }

        // 2. Year End Forecast
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        BigDecimal currentYearTotal = expenseRepository.findTotalSpentInPeriod(user, startOfYear, today);
        if (currentYearTotal == null) currentYearTotal = BigDecimal.ZERO;

        BigDecimal predictedYearEnd = BigDecimal.ZERO;
        int dayOfYear = today.getDayOfYear();
        int totalDaysInYear = today.lengthOfYear();

        if (dayOfYear > 0) {
            predictedYearEnd = currentYearTotal.divide(BigDecimal.valueOf(dayOfYear), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(totalDaysInYear));
        }

        // 3. Next Likely Spend
        // Get last 50 expenses to analyze pattern
        List<com.finance.concierge.entity.Expense> recentExpenses = expenseRepository.findByUserId(user.getId(), PageRequest.of(0, 50)).getContent();

        AnalyticsDTO.PredictedExpenseDTO nextLikely = predictNextExpense(recentExpenses);

        String analysis = String.format("Based on your spending habits, you are on track to spend $%s this month and $%s this year. Your most frequent expense category is %s.",
                predictedMonthEnd.toPlainString(),
                predictedYearEnd.toPlainString(),
                nextLikely != null ? nextLikely.getCategory() : "Unknown");

        return AnalyticsDTO.ForecastDTO.builder()
                .predictedMonthEndSpend(predictedMonthEnd)
                .predictedYearEndSpend(predictedYearEnd)
                .nextLikelySpend(nextLikely)
                .aiAnalysis(analysis)
                .build();
    }

    private AnalyticsDTO.PredictedExpenseDTO predictNextExpense(List<com.finance.concierge.entity.Expense> expenses) {
        if (expenses.isEmpty()) return null;

        // Group by category and count freq
        Map<String, Long> categoryFreq = expenses.stream()
                .collect(Collectors.groupingBy(e -> e.getCategory().getName(), Collectors.counting()));

        // Find most frequent
        String topCategory = categoryFreq.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("General");

        // Avg amount for this category
        double avgAmount = expenses.stream()
                .filter(e -> e.getCategory().getName().equals(topCategory))
                .mapToDouble(e -> e.getAmount().doubleValue())
                .average()
                .orElse(0.0);

        return AnalyticsDTO.PredictedExpenseDTO.builder()
                .category(topCategory)
                .estimatedAmount(BigDecimal.valueOf(avgAmount).setScale(2, RoundingMode.HALF_UP))
                .confidence("High")
                .build();
    }
}
