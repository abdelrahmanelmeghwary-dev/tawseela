package com.tawseela.service.impl;

import com.tawseela.config.TawseelaProperties;
import com.tawseela.dto.request.CreateOrderRequest;
import com.tawseela.dto.response.OrderHistoryResponse;
import com.tawseela.dto.response.OrderResponse;
import com.tawseela.dto.response.OrderStatusUpdateResponse;
import com.tawseela.dto.request.UpdateOrderStatusRequest;
import com.tawseela.entity.Driver;
import com.tawseela.entity.Order;
import com.tawseela.entity.OrderStatusHistory;
import com.tawseela.enums.OrderStatus;
import com.tawseela.mapper.OrderMapper;
import com.tawseela.repository.DriverRepository;
import com.tawseela.repository.OrderRepository;
import com.tawseela.repository.OrderStatusHistoryRepository;
import com.tawseela.service.OrderService;
import com.tawseela.service.OrderStateMachine;
import com.tawseela.service.PushNotificationService;
import com.tawseela.specification.OrderSpecifications;
import com.tawseela.enums.SystemRole;
import com.tawseela.entity.User;
import com.tawseela.exception.BusinessException;
import com.tawseela.exception.ResourceNotFoundException;
import com.tawseela.exception.UnauthorizedActionException;
import com.tawseela.repository.UserRepository;
import com.tawseela.security.SecurityUtils;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    private static final String NOTE_AUTO_EXPIRED = "auto_expired";
    private static final String NOTE_DRIVER_REJECTED = "driver_rejected";

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final OrderStateMachine stateMachine;
    private final PushNotificationService pushNotificationService;
    private final TawseelaProperties properties;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderStatusHistoryRepository historyRepository,
            DriverRepository driverRepository,
            UserRepository userRepository,
            OrderMapper orderMapper,
            OrderStateMachine stateMachine,
            PushNotificationService pushNotificationService,
            TawseelaProperties properties) {
        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
        this.driverRepository = driverRepository;
        this.userRepository = userRepository;
        this.orderMapper = orderMapper;
        this.stateMachine = stateMachine;
        this.pushNotificationService = pushNotificationService;
        this.properties = properties;
    }

    @Override
    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        if (!SecurityUtils.hasRole(SystemRole.CUSTOMER)) {
            throw new UnauthorizedActionException("Only customers can create orders");
        }
        UUID customerId = SecurityUtils.requireUserId();
        User customer = userRepository
                .findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = new Order();
        order.setCustomer(customer);
        order.setDescription(request.getDescription());
        order.setDeliveryLat(request.getDeliveryLat());
        order.setDeliveryLng(request.getDeliveryLng());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setStatus(OrderStatus.CREATED);
        order = orderRepository.save(order);
        appendHistory(order, OrderStatus.CREATED, customer, null);
        log.info("Order created id={} customerId={}", order.getId(), customerId);
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(UUID id) {
        return orderMapper.toResponse(loadAccessibleOrder(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> list(OrderStatus status, UUID customerId, UUID driverId, Pageable pageable) {
        if (customerId != null && !SecurityUtils.isAdmin()) {
            throw new UnauthorizedActionException("Only admin can filter by customerId");
        }
        if (driverId != null && !SecurityUtils.isAdmin()) {
            throw new UnauthorizedActionException("Only admin can filter by driverId");
        }
        Specification<Order> spec = Specification.allOf(
                OrderSpecifications.accessibleToCurrentUser(),
                OrderSpecifications.hasStatus(status),
                OrderSpecifications.hasCustomerId(customerId),
                OrderSpecifications.hasDriverId(driverId));
        return orderRepository.findAll(spec, pageable).map(orderMapper::toResponse);
    }

    @Override
    @Transactional
    public OrderStatusUpdateResponse updateStatus(UUID orderId, UpdateOrderStatusRequest request) {
        Order order = loadAccessibleOrder(orderId);
        OrderStatus target = request.getNewStatus();
        UUID actorId = SecurityUtils.requireUserId();
        String note = request.getNote() != null ? request.getNote() : "";

        stateMachine.assertTransition(order, target, actorId);

        if (target == OrderStatus.ASSIGNED && request.getDriverId() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "driverId required for ASSIGNED");
        }

        OrderStatus current = order.getStatus();
        applyStatusChange(order, target, request.getDriverId(), note, current);

        User actor = userRepository.findById(actorId).orElse(null);
        UUID historyActorId = NOTE_AUTO_EXPIRED.equals(note) ? null : actorId;
        appendHistory(order, target, historyActorId != null ? actor : null, note.isBlank() ? null : note);

        if (target == OrderStatus.COMPLETED && order.getDriver() != null) {
            Driver driver = order.getDriver();
            driver.setTotalDeliveries(driver.getTotalDeliveries() + 1);
            driverRepository.save(driver);
        }

        order = orderRepository.save(order);
        dispatchStatusPush(order, target, note);
        log.info("Order status updated id={} {} -> {}", orderId, current, target);
        return OrderStatusUpdateResponse.builder()
                .success(true)
                .order(orderMapper.toResponse(order))
                .build();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!SecurityUtils.isAdmin()) {
            throw new UnauthorizedActionException("Only admin can delete orders");
        }
        Order order = orderRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        orderRepository.delete(order);
        log.info("Order deleted id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderHistoryResponse> history(UUID orderId) {
        loadAccessibleOrder(orderId);
        return historyRepository.findByOrderIdOrderByCreatedAtAsc(orderId).stream()
                .map(orderMapper::toHistoryResponse)
                .toList();
    }

    @Override
    @Transactional
    public int expireStaleAssignments() {
        int seconds = properties.getDelivery().getStaleAssignmentSeconds();
        Instant cutoff = Instant.now().minus(seconds, ChronoUnit.SECONDS);
        List<Order> stale = orderRepository.findByStatusAndAssignedAtBefore(OrderStatus.ASSIGNED, cutoff);
        for (Order order : stale) {
            order.setStatus(OrderStatus.CREATED);
            order.setDriver(null);
            order.setAssignedAt(null);
            orderRepository.save(order);
            appendHistory(order, OrderStatus.CREATED, null, NOTE_AUTO_EXPIRED);
            dispatchStatusPush(order, OrderStatus.CREATED, NOTE_AUTO_EXPIRED);
            log.info("Expired stale assignment orderId={}", order.getId());
        }
        return stale.size();
    }

    private void dispatchStatusPush(Order order, OrderStatus target, String note) {
        UUID orderId = order.getId();
        Map<String, String> data = Map.of("orderId", orderId.toString());
        switch (target) {
            case ASSIGNED -> {
                if (order.getDriver() != null) {
                    pushNotificationService.notifyUser(
                            order.getDriver().getId(),
                            "📦 New Delivery",
                            "You have a new order request",
                            data);
                }
            }
            case ACCEPTED -> pushNotificationService.notifyUser(
                    order.getCustomer().getId(),
                    "✅ Order Accepted",
                    "Your driver is heading to the store",
                    data);
            case ON_THE_WAY -> pushNotificationService.notifyUser(
                    order.getCustomer().getId(),
                    "🛵 On The Way",
                    "Your order is coming",
                    data);
            case DELIVERED -> pushNotificationService.notifyUser(
                    order.getCustomer().getId(),
                    "🎉 Delivered",
                    "Your order has been delivered",
                    data);
            case CREATED -> {
                if (NOTE_AUTO_EXPIRED.equals(note)) {
                    String shortId = orderId.toString().substring(0, 8);
                    notifyAdmins("⏰ No Response", "Order #" + shortId + " was not accepted, back in queue", data);
                }
            }
            default -> {}
        }
    }

    private void notifyAdmins(String title, String body, Map<String, String> data) {
        userRepository.findAllWithRoles().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName() == SystemRole.ADMIN))
                .forEach(admin -> pushNotificationService.notifyUser(admin.getId(), title, body, data));
    }

    private void applyStatusChange(
            Order order, OrderStatus target, UUID driverId, String note, OrderStatus current) {
        order.setStatus(target);
        if (target == OrderStatus.ASSIGNED) {
            Driver driver = driverRepository
                    .findById(driverId)
                    .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
            order.setDriver(driver);
            order.setAssignedAt(Instant.now());
        }
        if (target == OrderStatus.CREATED
                && (current == OrderStatus.ASSIGNED
                        || NOTE_AUTO_EXPIRED.equals(note)
                        || NOTE_DRIVER_REJECTED.equals(note))) {
            order.setDriver(null);
            order.setAssignedAt(null);
        }
    }

    private Order loadAccessibleOrder(UUID id) {
        Order order = orderRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (SecurityUtils.isAdmin()) {
            return order;
        }
        UUID userId = SecurityUtils.requireUserId();
        if (SecurityUtils.hasRole(SystemRole.CUSTOMER)
                && order.getCustomer().getId().equals(userId)) {
            return order;
        }
        if (SecurityUtils.hasRole(SystemRole.DRIVER)
                && order.getDriver() != null
                && order.getDriver().getId().equals(userId)) {
            return order;
        }
        throw new UnauthorizedActionException("Access denied to order");
    }

    private void appendHistory(Order order, OrderStatus status, User actor, String note) {
        OrderStatusHistory row = new OrderStatusHistory();
        row.setOrder(order);
        row.setStatus(status);
        row.setActor(actor);
        row.setNote(note);
        historyRepository.save(row);
    }
}
