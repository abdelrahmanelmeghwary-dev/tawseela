package com.tawseela.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.tawseela.config.TawseelaProperties;
import com.tawseela.dto.response.PushSendResponse;
import com.tawseela.entity.Profile;
import com.tawseela.repository.ProfileRepository;
import com.tawseela.service.FcmPushService;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Service
public class FcmPushServiceImpl implements FcmPushService {

    private static final Logger log = LoggerFactory.getLogger(FcmPushServiceImpl.class);
    private static final String FCM_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String LEGACY_URL = "https://fcm.googleapis.com/fcm/send";

    private final TawseelaProperties properties;
    private final ProfileRepository profileRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public FcmPushServiceImpl(
            TawseelaProperties properties,
            ProfileRepository profileRepository,
            ObjectMapper objectMapper,
            RestTemplate restTemplate) {
        this.properties = properties;
        this.profileRepository = profileRepository;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    @Override
    public PushSendResponse send(UUID userId, String title, String body, Map<String, String> data) {
        Profile profile = profileRepository.findById(userId).orElse(null);
        if (profile == null || !StringUtils.hasText(profile.getFcmToken())) {
            return PushSendResponse.builder().success(false).reason("no_fcm_token").build();
        }
        String token = profile.getFcmToken();
        String serviceAccountJson = properties.getFcm().getServiceAccountJson();
        if (StringUtils.hasText(serviceAccountJson)) {
            return sendV1(serviceAccountJson, token, title, body, data);
        }
        String serverKey = properties.getFcm().getServerKey();
        if (StringUtils.hasText(serverKey)) {
            return sendLegacy(serverKey, token, title, body, data);
        }
        return PushSendResponse.builder().success(false).reason("no_fcm_config").build();
    }

    private PushSendResponse sendV1(
            String serviceAccountJson, String token, String title, String body, Map<String, String> data) {
        try {
            JsonNode sa = objectMapper.readTree(serviceAccountJson);
            String projectId = sa.path("project_id").asText(null);
            String clientEmail = sa.path("client_email").asText(null);
            String privateKey = sa.path("private_key").asText(null);
            if (!StringUtils.hasText(projectId) || !StringUtils.hasText(clientEmail) || !StringUtils.hasText(privateKey)) {
                return PushSendResponse.builder().success(false).reason("missing_fields").build();
            }
            privateKey = privateKey.replace("\\n", "\n");
            GoogleCredentials credentials = ServiceAccountCredentials.fromPkcs8(
                    clientEmail, null, privateKey, null, Collections.singleton(FCM_SCOPE));
            credentials.refreshIfExpired();
            String accessToken = credentials.getAccessToken().getTokenValue();

            ObjectNode message = objectMapper.createObjectNode();
            message.put("token", token);
            message.set("notification", objectMapper.createObjectNode().put("title", title).put("body", body));
            ObjectNode dataNode = objectMapper.createObjectNode();
            if (data != null) {
                data.forEach(dataNode::put);
            }
            message.set("data", dataNode);

            ObjectNode payload = objectMapper.createObjectNode().set("message", message);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            String url = "https://fcm.googleapis.com/v1/projects/" + projectId + "/messages:send";
            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, new HttpEntity<>(payload.toString(), headers), String.class);
            boolean ok = response.getStatusCode().is2xxSuccessful();
            if (!ok) {
                log.warn("FCM v1 failed status={} body={}", response.getStatusCode(), response.getBody());
            }
            return PushSendResponse.builder().success(ok).build();
        } catch (Exception e) {
            log.warn("FCM v1 error: {}", e.getMessage());
            return PushSendResponse.builder().success(false).reason("token_error").build();
        }
    }

    private PushSendResponse sendLegacy(
            String serverKey, String token, String title, String body, Map<String, String> data) {
        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("to", token);
            payload.set("notification", objectMapper.createObjectNode().put("title", title).put("body", body));
            ObjectNode dataNode = objectMapper.createObjectNode();
            if (data != null) {
                data.forEach(dataNode::put);
            }
            payload.set("data", dataNode);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "key=" + serverKey);
            ResponseEntity<String> response =
                    restTemplate.postForEntity(LEGACY_URL, new HttpEntity<>(payload.toString(), headers), String.class);
            return PushSendResponse.builder().success(response.getStatusCode().is2xxSuccessful()).build();
        } catch (Exception e) {
            log.warn("FCM legacy error: {}", e.getMessage());
            return PushSendResponse.builder().success(false).reason("fcm_not_configured").build();
        }
    }
}
