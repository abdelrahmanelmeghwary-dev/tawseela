package com.tawseela.service;

import com.tawseela.dto.response.PushSendResponse;
import com.tawseela.dto.request.SendPushRequest;
import java.util.Map;
import java.util.UUID;

public interface PushNotificationService {

    PushSendResponse send(SendPushRequest request);

    void notifyUser(UUID userId, String title, String body, Map<String, String> data);
}
