package com.tawseela.specification;

import com.tawseela.entity.Notification;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class NotificationSpecifications {

    private NotificationSpecifications() {}

    public static Specification<Notification> forUser(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Notification> isRead(Boolean read) {
        if (read == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.equal(root.get("read"), read);
    }
}
