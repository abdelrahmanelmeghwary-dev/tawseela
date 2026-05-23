package com.tawseela.mapper;

import com.tawseela.dto.response.OrderHistoryResponse;
import com.tawseela.dto.response.OrderResponse;
import com.tawseela.entity.Order;
import com.tawseela.entity.OrderStatusHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "driverId", source = "driver.id")
    OrderResponse toResponse(Order order);

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "actorId", source = "actor.id")
    OrderHistoryResponse toHistoryResponse(OrderStatusHistory history);
}
