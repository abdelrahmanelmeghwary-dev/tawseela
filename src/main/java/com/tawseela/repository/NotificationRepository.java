package com.tawseela.repository;

import com.tawseela.entity.Notification;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NotificationRepository
        extends JpaRepository<Notification, UUID>, JpaSpecificationExecutor<Notification> {}
