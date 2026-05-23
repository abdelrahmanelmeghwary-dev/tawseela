package com.tawseela.dto.response;

import com.tawseela.enums.OrderStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrderHistoryResponse {
    UUID id;
    UUID orderId;
    OrderStatus status;
    UUID actorId;
    String note;
    Instant createdAt;
}
