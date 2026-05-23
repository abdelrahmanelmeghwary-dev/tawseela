package com.tawseela.controller;

import com.tawseela.dto.request.CreateNotificationRequest;
import com.tawseela.dto.response.NotificationResponse;
import com.tawseela.dto.request.UpdateNotificationRequest;
import com.tawseela.service.NotificationService;
import com.tawseela.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List my notifications")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> list(
            @RequestParam(required = false) Boolean isRead,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.list(isRead, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SERVICE')")
    @Operation(summary = "Create notification (admin/service)")
    public ResponseEntity<ApiResponse<NotificationResponse>> create(
            @Valid @RequestBody CreateNotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(notificationService.create(request)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark notification read/unread")
    public ResponseEntity<ApiResponse<NotificationResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody UpdateNotificationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.markRead(id, request)));
    }
}
