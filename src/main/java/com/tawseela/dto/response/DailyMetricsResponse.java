package com.tawseela.dto.response;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DailyMetricsResponse {
    LocalDate date;
    long totalOrders;
    long completedOrders;
    long cancelledOrders;
    long activeDrivers;
}
