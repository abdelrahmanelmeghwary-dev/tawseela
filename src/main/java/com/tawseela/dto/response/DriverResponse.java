package com.tawseela.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DriverResponse {
    UUID id;
    boolean online;
    Instant lastSeen;
    BigDecimal currentLat;
    BigDecimal currentLng;
    int totalDeliveries;
    Instant createdAt;
    Instant updatedAt;
    DriverProfileSummary profile;
}
