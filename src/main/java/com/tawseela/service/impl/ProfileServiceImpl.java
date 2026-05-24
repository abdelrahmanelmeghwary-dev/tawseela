package com.tawseela.service.impl;

import com.tawseela.dto.request.CreateProfileRequest;
import com.tawseela.dto.response.ProfileResponse;
import com.tawseela.dto.request.UpdateProfileRequest;
import com.tawseela.entity.Profile;
import com.tawseela.mapper.ProfileMapper;
import com.tawseela.repository.ProfileRepository;
import com.tawseela.service.ProfileService;
import com.tawseela.specification.ProfileSpecifications;
import com.tawseela.enums.SystemRole;
import com.tawseela.entity.User;
import com.tawseela.exception.BusinessException;
import com.tawseela.exception.ResourceNotFoundException;
import com.tawseela.exception.UnauthorizedActionException;
import com.tawseela.repository.UserRepository;
import com.tawseela.security.SecurityUtils;
import com.tawseela.util.RoleNames;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ProfileServiceImpl implements ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileServiceImpl.class);

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ProfileMapper profileMapper;

    public ProfileServiceImpl(
            ProfileRepository profileRepository,
            UserRepository userRepository,
            ProfileMapper profileMapper) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.profileMapper = profileMapper;
    }

    @Override
    @Transactional
    public Profile ensureProfile(UUID userId) {
        return profileRepository
                .findByIdWithUserAndRoles(userId)
                .orElseGet(() -> provisionProfile(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProfileResponse> list(SystemRole role, Pageable pageable) {
        if (!SecurityUtils.isAdmin()) {
            throw new UnauthorizedActionException("Only admin can list profiles");
        }
        Specification<Profile> spec = ProfileSpecifications.hasUserRole(role);
        return profileRepository.findAll(spec, pageable).map(profile -> {
            if (profile.getUser() == null || profile.getUser().getRoles().isEmpty()) {
                profileRepository.findByIdWithUserAndRoles(profile.getId()).ifPresent(p -> profile.setUser(p.getUser()));
            }
            return profileMapper.toResponse(profile);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getById(UUID id) {
        Profile profile = loadAccessible(id);
        return profileMapper.toResponse(profile);
    }

    @Override
    @Transactional
    public ProfileResponse create(CreateProfileRequest request) {
        UUID userId = request.getId() != null ? request.getId() : SecurityUtils.requireUserId();
        if (!userId.equals(SecurityUtils.requireUserId())) {
            throw new UnauthorizedActionException("Profile id must match authenticated user");
        }
        if (profileRepository.existsById(userId)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Profile already exists");
        }
        User user = userRepository
                .findByIdEagerRoles(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Profile profile = new Profile();
        profile.setId(userId);
        profile.setUser(user);
        profile.setFullName(request.getFullName());
        profile.setPhone(request.getPhone());
        profile = profileRepository.save(profile);
        log.info("Profile created userId={}", userId);
        return profileMapper.toResponse(profile);
    }

    @Override
    @Transactional
    public ProfileResponse update(UUID id, UpdateProfileRequest request) {
        Profile profile = loadAccessible(id);
        if (!id.equals(SecurityUtils.requireUserId())) {
            throw new UnauthorizedActionException("You can only update your own profile");
        }
        if (StringUtils.hasText(request.getFullName())) {
            profile.setFullName(request.getFullName());
        }
        if (request.getFcmToken() != null) {
            profile.setFcmToken(request.getFcmToken());
        }
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }
        profile = profileRepository.save(profile);
        return profileMapper.toResponse(profile);
    }

    private Profile provisionProfile(UUID userId) {
        User user = userRepository
                .findByIdEagerRoles(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Profile profile = new Profile();
        profile.setId(userId);
        profile.setUser(user);
        profile.setFullName(user.getFirstName() + " " + user.getLastName());
        profile.setPhone(user.getMobileNumber());
        profile.setRole(legacyRoleFor(user));
        profile = profileRepository.save(profile);
        log.info("Auto-provisioned profile userId={}", userId);
        return profile;
    }

    private static String legacyRoleFor(User user) {
        if (RoleNames.hasRole(user, SystemRole.DRIVER)) {
            return "driver";
        }
        if (RoleNames.hasRole(user, SystemRole.ADMIN)) {
            return "admin";
        }
        return "customer";
    }

    private Profile loadAccessible(UUID id) {
        Profile profile = profileRepository
                .findByIdWithUserAndRoles(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        if (SecurityUtils.isAdmin() || id.equals(SecurityUtils.requireUserId())) {
            return profile;
        }
        throw new UnauthorizedActionException("Access denied to profile");
    }
}
