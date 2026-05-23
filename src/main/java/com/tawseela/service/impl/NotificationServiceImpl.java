package com.tawseela.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tawseela.dto.request.CreateNotificationRequest;
import com.tawseela.dto.response.NotificationResponse;
import com.tawseela.dto.request.UpdateNotificationRequest;
import com.tawseela.entity.Notification;
import com.tawseela.mapper.NotificationMapper;
import com.tawseela.repository.NotificationRepository;
import com.tawseela.service.NotificationService;
import com.tawseela.specification.NotificationSpecifications;
import com.tawseela.entity.User;
import com.tawseela.exception.ResourceNotFoundException;
import com.tawseela.exception.UnauthorizedActionException;
import com.tawseela.repository.UserRepository;
import com.tawseela.security.SecurityUtils;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final ObjectMapper objectMapper;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            NotificationMapper notificationMapper,
            ObjectMapper objectMapper) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationMapper = notificationMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> list(Boolean isRead, Pageable pageable) {
        UUID userId = SecurityUtils.requireUserId();
        Specification<Notification> spec = Specification.allOf(
                NotificationSpecifications.forUser(userId), NotificationSpecifications.isRead(isRead));
        return notificationRepository.findAll(spec, pageable).map(notificationMapper::toResponse);
    }

    @Override
    @Transactional
    public NotificationResponse markRead(UUID id, UpdateNotificationRequest request) {
        UUID userId = SecurityUtils.requireUserId();
        Notification notification = notificationRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getUser().getId().equals(userId)) {
            throw new UnauthorizedActionException("Access denied to notification");
        }
        notification.setRead(Boolean.TRUE.equals(request.getIsRead()));
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public NotificationResponse create(CreateNotificationRequest request) {
        return createInternal(
                request.getUserId(),
                request.getTitle(),
                request.getBody(),
                request.getData() != null ? request.getData() : Map.of());
    }

    @Override
    @Transactional
    public NotificationResponse createInternal(UUID userId, String title, String body, Map<String, String> data) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setRead(false);
        try {
            notification.setData(objectMapper.writeValueAsString(data != null ? data : Map.of()));
        } catch (Exception e) {
            notification.setData("{}");
        }
        notification = notificationRepository.save(notification);
        log.debug("In-app notification created userId={} title={}", userId, title);
        return notificationMapper.toResponse(notification);
    }
}
