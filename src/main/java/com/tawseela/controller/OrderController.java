package com.tawseela.controller;

import com.tawseela.dto.request.CreateOrderRequest;
import com.tawseela.dto.response.OrderHistoryResponse;
import com.tawseela.dto.response.OrderResponse;
import com.tawseela.dto.response.OrderStatusUpdateResponse;
import com.tawseela.dto.request.UpdateOrderStatusRequest;
import com.tawseela.enums.OrderStatus;
import com.tawseela.service.OrderService;
import com.tawseela.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create order")
    public ResponseEntity<ApiResponse<OrderResponse>> create(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(orderService.create(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','DRIVER','ADMIN')")
    @Operation(summary = "List orders")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> list(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID driverId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.list(status, customerId, driverId, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER','DRIVER','ADMIN')")
    @Operation(summary = "Get order by id")
    public ResponseEntity<ApiResponse<OrderResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getById(id)));
    }

    @PostMapping("/{orderId}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update order status")
    public ResponseEntity<ApiResponse<OrderStatusUpdateResponse>> updateStatus(
            @PathVariable UUID orderId, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.updateStatus(orderId, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete order")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{orderId}/history")
    @PreAuthorize("hasAnyRole('CUSTOMER','DRIVER','ADMIN')")
    @Operation(summary = "Order status history")
    public ResponseEntity<ApiResponse<List<OrderHistoryResponse>>> history(@PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.history(orderId)));
    }
}
