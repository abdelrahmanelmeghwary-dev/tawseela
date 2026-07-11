package com.tawseela.mapper;

import com.tawseela.dto.response.CustomerSummary;
import com.tawseela.dto.response.DriverSummary;
import com.tawseela.dto.response.OrderHistoryResponse;
import com.tawseela.dto.response.OrderResponse;
import com.tawseela.entity.Driver;
import com.tawseela.entity.Order;
import com.tawseela.entity.OrderStatusHistory;
import com.tawseela.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "customer", expression = "java(toCustomerSummary(order.getCustomer()))")
    @Mapping(target = "driver", expression = "java(toDriverSummary(order.getDriver()))")
    OrderResponse toResponse(Order order);

    default CustomerSummary toCustomerSummary(User user) {
        if (user == null) return null;
        return CustomerSummary.builder()
                .id(user.getId())
                .name(user.getFirstName() + " " + user.getLastName())
                .mobileNumber(user.getMobileNumber())
                .build();
    }

    default DriverSummary toDriverSummary(Driver driver) {
        if (driver == null) return null;
        User user = driver.getUser();
        if (user == null) return DriverSummary.builder().id(driver.getId()).build();
        return DriverSummary.builder()
                .id(driver.getId())
                .name(user.getFirstName() + " " + user.getLastName())
                .mobileNumber(user.getMobileNumber())
                .build();
    }

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "actorId", source = "actor.id")
    OrderHistoryResponse toHistoryResponse(OrderStatusHistory history);
}
