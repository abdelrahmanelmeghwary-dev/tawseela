package com.tawseela.specification;

import com.tawseela.entity.Order;
import com.tawseela.enums.OrderStatus;
import com.tawseela.enums.SystemRole;
import com.tawseela.security.SecurityUtils;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class OrderSpecifications {

    private OrderSpecifications() {}

    public static Specification<Order> accessibleToCurrentUser() {
        if (SecurityUtils.isAdmin()) {
            return (root, query, cb) -> cb.conjunction();
        }
        UUID userId = SecurityUtils.requireUserId();
        if (SecurityUtils.hasRole(SystemRole.DRIVER)) {
            return (root, query, cb) -> cb.equal(root.get("driver").get("id"), userId);
        }
        if (SecurityUtils.hasRole(SystemRole.CUSTOMER)) {
            return (root, query, cb) -> cb.equal(root.get("customer").get("id"), userId);
        }
        return (root, query, cb) -> cb.disjunction();
    }

    public static Specification<Order> hasStatus(OrderStatus status) {
        if (status == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Order> hasCustomerId(UUID customerId) {
        if (customerId == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.equal(root.get("customer").get("id"), customerId);
    }

    public static Specification<Order> hasDriverId(UUID driverId) {
        if (driverId == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.equal(root.get("driver").get("id"), driverId);
    }
}
