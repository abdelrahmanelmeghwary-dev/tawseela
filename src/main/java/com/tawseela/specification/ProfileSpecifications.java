package com.tawseela.specification;

import com.tawseela.entity.Profile;
import com.tawseela.enums.SystemRole;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public final class ProfileSpecifications {

    private ProfileSpecifications() {}

    public static Specification<Profile> hasUserRole(SystemRole role) {
        if (role == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.equal(
                    root.join("user", JoinType.INNER).join("roles", JoinType.INNER).get("name"), role);
        };
    }
}
