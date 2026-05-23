package com.tawseela.service;

import com.tawseela.entity.Order;
import com.tawseela.enums.OrderStatus;
import com.tawseela.enums.SystemRole;
import com.tawseela.exception.BusinessException;
import com.tawseela.security.SecurityUtils;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class OrderStateMachine {

    private static final String ROLE_ADMIN = "admin";
    private static final String ROLE_DRIVER = "driver";
    private static final String ROLE_SYSTEM = "system";

    private static final Map<OrderStatus, List<OrderStatus>> NEXT = Map.of(
            OrderStatus.CREATED, List.of(OrderStatus.ASSIGNED),
            OrderStatus.ASSIGNED, List.of(OrderStatus.ACCEPTED, OrderStatus.CREATED),
            OrderStatus.ACCEPTED, List.of(OrderStatus.PURCHASING),
            OrderStatus.PURCHASING, List.of(OrderStatus.ON_THE_WAY),
            OrderStatus.ON_THE_WAY, List.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED, List.of(OrderStatus.COMPLETED),
            OrderStatus.COMPLETED, List.of(),
            OrderStatus.CANCELLED, List.of());

    private static final Map<OrderStatus, List<String>> ROLES = Map.of(
            OrderStatus.CREATED, List.of(ROLE_ADMIN),
            OrderStatus.ASSIGNED, List.of(ROLE_DRIVER, ROLE_SYSTEM),
            OrderStatus.ACCEPTED, List.of(ROLE_DRIVER),
            OrderStatus.PURCHASING, List.of(ROLE_DRIVER),
            OrderStatus.ON_THE_WAY, List.of(ROLE_DRIVER),
            OrderStatus.DELIVERED, List.of(ROLE_DRIVER, ROLE_SYSTEM),
            OrderStatus.COMPLETED, List.of(),
            OrderStatus.CANCELLED, List.of());

    public void assertTransition(Order order, OrderStatus target, UUID actorId) {
        if (target == OrderStatus.CANCELLED) {
            if (!SecurityUtils.isAdmin()) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "Only admin can cancel orders");
            }
            return;
        }
        OrderStatus current = order.getStatus();
        String effectiveRole = resolveEffectiveRole(target);
        if (!canTransition(current, target, effectiveRole, actorId, order)
                && !canTransition(current, target, ROLE_SYSTEM, actorId, order)) {
            throw new BusinessException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Transition from " + current + " to " + target + " is not allowed");
        }
    }

    private String resolveEffectiveRole(OrderStatus target) {
        if (SecurityUtils.isAdmin() && target == OrderStatus.ASSIGNED) {
            return ROLE_ADMIN;
        }
        if (SecurityUtils.hasRole(SystemRole.DRIVER)) {
            return ROLE_DRIVER;
        }
        if (SecurityUtils.isAdmin()) {
            return ROLE_ADMIN;
        }
        return SystemRole.CUSTOMER.name().toLowerCase();
    }

    private boolean canTransition(
            OrderStatus from, OrderStatus to, String role, UUID actorId, Order order) {
        if (to == OrderStatus.CANCELLED) {
            return ROLE_ADMIN.equals(role);
        }
        List<OrderStatus> next = NEXT.get(from);
        List<String> roles = ROLES.get(from);
        if (next == null || roles == null || !next.contains(to) || !roles.contains(role)) {
            return false;
        }
        if (ROLE_DRIVER.equals(role)) {
            return order.getDriver() != null && order.getDriver().getId().equals(actorId);
        }
        return true;
    }
}
