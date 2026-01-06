package com.finance.concierge.service;

import com.finance.concierge.dto.AnalyticsDTO;
import com.finance.concierge.entity.User;

import java.util.List;

public interface AnalyticsService {

    List<AnalyticsDTO.DailyTrendDTO> getDailyTrend(User user, int days);

    List<AnalyticsDTO.MonthlySpendDTO> getMonthlySpend(User user, int year);

    AnalyticsDTO.SummaryDTO getAnalyticsSummary(User user);

    AnalyticsDTO.ForecastDTO getForecast(User user);
}
