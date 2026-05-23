package com.tawseela.service;

import com.tawseela.dto.response.PushSendResponse;
import java.util.Map;
import java.util.UUID;

public interface FcmPushService {

    PushSendResponse send(UUID userId, String title, String body, Map<String, String> data);
}
