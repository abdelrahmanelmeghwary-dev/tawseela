package com.tawseela.controller;

import com.tawseela.dto.response.PushSendResponse;
import com.tawseela.dto.request.SendPushRequest;
import com.tawseela.service.PushNotificationService;
import com.tawseela.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/push")
@Tag(name = "Push")
public class PushController {

    private final PushNotificationService pushNotificationService;

    public PushController(PushNotificationService pushNotificationService) {
        this.pushNotificationService = pushNotificationService;
    }

    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('ADMIN','SERVICE')")
    @Operation(summary = "Send FCM push to user")
    public ResponseEntity<ApiResponse<PushSendResponse>> send(@Valid @RequestBody SendPushRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(pushNotificationService.send(request)));
    }
}
