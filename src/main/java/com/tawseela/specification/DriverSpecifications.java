package com.tawseela.specification;

import com.tawseela.entity.Driver;
import org.springframework.data.jpa.domain.Specification;

public final class DriverSpecifications {

    private DriverSpecifications() {}

    public static Specification<Driver> isOnline(Boolean online) {
        if (online == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.equal(root.get("online"), online);
    }
}
