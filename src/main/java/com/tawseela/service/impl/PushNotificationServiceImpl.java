package com.tawseela.service.impl;

import com.tawseela.dto.response.PushSendResponse;
import com.tawseela.dto.request.SendPushRequest;
import com.tawseela.service.FcmPushService;
import com.tawseela.service.PushNotificationService;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PushNotificationServiceImpl implements PushNotificationService {

    private final FcmPushService fcmPushService;

    public PushNotificationServiceImpl(FcmPushService fcmPushService) {
        this.fcmPushService = fcmPushService;
    }

    @Override
    public PushSendResponse send(SendPushRequest request) {
        return fcmPushService.send(
                request.getUserId(),
                request.getTitle(),
                request.getBody(),
                request.getData() != null ? request.getData() : Map.of());
    }

    @Override
    public void notifyUser(UUID userId, String title, String body, Map<String, String> data) {
        fcmPushService.send(userId, title, body, data);
    }
}
