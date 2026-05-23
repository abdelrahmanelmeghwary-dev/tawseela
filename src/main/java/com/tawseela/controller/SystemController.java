package com.tawseela.controller;

import com.tawseela.service.OrderService;
import com.tawseela.common.ApiResponse;
import io.swagger.v3.oas.annotations.Hidden;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
@Hidden
public class SystemController {

    private final OrderService orderService;

    public SystemController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/expire-stale-assignments")
    @PreAuthorize("hasRole('CRON')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> expireStaleAssignments() {
        int expired = orderService.expireStaleAssignments();
        return ResponseEntity.ok(ApiResponse.ok(Map.of("success", true, "expired", expired)));
    }
}
