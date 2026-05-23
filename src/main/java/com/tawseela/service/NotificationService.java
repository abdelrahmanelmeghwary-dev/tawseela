package com.tawseela.service;

import com.tawseela.dto.request.CreateNotificationRequest;
import com.tawseela.dto.response.NotificationResponse;
import com.tawseela.dto.request.UpdateNotificationRequest;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    Page<NotificationResponse> list(Boolean isRead, Pageable pageable);

    NotificationResponse markRead(UUID id, UpdateNotificationRequest request);

    NotificationResponse create(CreateNotificationRequest request);

    NotificationResponse createInternal(UUID userId, String title, String body, Map<String, String> data);
}
