package com.tawseela.repository;

import com.tawseela.entity.Profile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfileRepository extends JpaRepository<Profile, UUID>, JpaSpecificationExecutor<Profile> {

    @Query("SELECT p FROM Profile p JOIN FETCH p.user u LEFT JOIN FETCH u.roles WHERE p.id = :id")
    Optional<Profile> findByIdWithUserAndRoles(@Param("id") UUID id);
}
