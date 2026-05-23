package com.tawseela.service;

import com.tawseela.dto.response.DailyMetricsResponse;
import java.time.LocalDate;

public interface MetricsService {

    DailyMetricsResponse getDailyMetrics(LocalDate date);
}
