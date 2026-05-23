package com.tawseela.service.impl;

import com.tawseela.config.TawseelaProperties;
import com.tawseela.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StaleAssignmentScheduler {

    private static final Logger log = LoggerFactory.getLogger(StaleAssignmentScheduler.class);

    private final OrderService orderService;
    private final TawseelaProperties properties;

    public StaleAssignmentScheduler(OrderService orderService, TawseelaProperties properties) {
        this.orderService = orderService;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${tawseela.delivery.expire-assignments-fixed-delay-ms:60000}")
    public void expireStaleAssignments() {
        int expired = orderService.expireStaleAssignments();
        if (expired > 0) {
            log.info("Scheduled expire stale assignments count={}", expired);
        }
    }
}
