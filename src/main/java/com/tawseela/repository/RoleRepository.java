package com.tawseela.repository;

import com.tawseela.entity.RoleEntity;
import com.tawseela.entity.SystemRole;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByName(SystemRole name);
}
