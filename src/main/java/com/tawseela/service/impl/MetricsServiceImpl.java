package com.tawseela.service.impl;

import com.tawseela.dto.response.DailyMetricsResponse;
import com.tawseela.repository.MetricsRepository;
import com.tawseela.service.MetricsService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MetricsServiceImpl implements MetricsService {

    private final MetricsRepository metricsRepository;

    public MetricsServiceImpl(MetricsRepository metricsRepository) {
        this.metricsRepository = metricsRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DailyMetricsResponse getDailyMetrics(LocalDate date) {
        LocalDate target = date != null ? date : LocalDate.now();
        List<Object[]> rows = metricsRepository.countOrderMetrics(target);
        long total = 0;
        long completed = 0;
        long cancelled = 0;
        if (!rows.isEmpty() && rows.getFirst() != null) {
            Object[] row = rows.getFirst();
            total = toLong(row[0]);
            completed = toLong(row[1]);
            cancelled = toLong(row[2]);
        }
        long activeDrivers = metricsRepository.countOnlineDrivers();
        return DailyMetricsResponse.builder()
                .date(target)
                .totalOrders(total)
                .completedOrders(completed)
                .cancelledOrders(cancelled)
                .activeDrivers(activeDrivers)
                .build();
    }

    private static long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        return ((Number) value).longValue();
    }
}
