package com.tawseela.repository;

import com.tawseela.domain.Profile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    Optional<Profile> findByPhone(String phone);

    Optional<Profile> findByEmailIgnoreCase(String email);
}
