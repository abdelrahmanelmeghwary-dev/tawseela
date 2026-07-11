package com.tawseela.dto.response;

import com.tawseela.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrderResponse {
    UUID id;
    CustomerSummary customer;
    DriverSummary driver;
    String description;
    BigDecimal deliveryLat;
    BigDecimal deliveryLng;
    String deliveryAddress;
    OrderStatus status;
    Instant assignedAt;
    Instant createdAt;
    Instant updatedAt;
}
