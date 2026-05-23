package com.tawseela.service;

import com.tawseela.dto.request.CreateOrderRequest;
import com.tawseela.dto.response.OrderHistoryResponse;
import com.tawseela.dto.response.OrderResponse;
import com.tawseela.dto.response.OrderStatusUpdateResponse;
import com.tawseela.dto.request.UpdateOrderStatusRequest;
import com.tawseela.enums.OrderStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponse create(CreateOrderRequest request);

    OrderResponse getById(UUID id);

    Page<OrderResponse> list(OrderStatus status, UUID customerId, UUID driverId, Pageable pageable);

    OrderStatusUpdateResponse updateStatus(UUID orderId, UpdateOrderStatusRequest request);

    void delete(UUID id);

    List<OrderHistoryResponse> history(UUID orderId);

    int expireStaleAssignments();
}
