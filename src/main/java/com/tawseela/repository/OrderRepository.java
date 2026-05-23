package com.tawseela.repository;

import com.tawseela.entity.Order;
import com.tawseela.enums.OrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    List<Order> findByStatusAndAssignedAtBefore(OrderStatus status, Instant assignedBefore);
}
