package com.tawseela.util;

import com.tawseela.entity.User;
import com.tawseela.enums.SystemRole;
import java.util.List;

public final class RoleNames {

    private RoleNames() {}

    public static List<String> fromUser(User user) {
        return user.getRoles().stream().map(r -> r.getName().name()).toList();
    }

    public static boolean hasRole(User user, SystemRole role) {
        return user.getRoles().stream().anyMatch(r -> r.getName() == role);
    }

    public static String primaryRole(User user) {
        return user.getRoles().stream()
                .findFirst()
                .map(r -> r.getName().name())
                .orElse("CUSTOMER");
    }
}
