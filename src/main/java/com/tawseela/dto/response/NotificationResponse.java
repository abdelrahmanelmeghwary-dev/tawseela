package com.tawseela.dto.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NotificationResponse {
    UUID id;
    UUID userId;
    String title;
    String body;
    Map<String, String> data;
    boolean read;
    Instant createdAt;
}
