package com.tawseela.service;

import com.tawseela.domain.Profile;
import com.tawseela.domain.Role;
import com.tawseela.repository.ProfileRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Profile> findById(UUID id) {
        return profileRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Profile> findByPhone(String phone) {
        return profileRepository.findByPhone(phone);
    }

    @Transactional
    public Profile save(Profile profile) {
        return profileRepository.save(profile);
    }

    /** For admin tools or SQL-backed promotion — not exposed on public OTP verify. */
    @Transactional
    public Profile updateRole(UUID id, Role newRole) {
        Profile p = profileRepository.findById(id).orElseThrow();
        p.setRole(newRole);
        return profileRepository.save(p);
    }
}
