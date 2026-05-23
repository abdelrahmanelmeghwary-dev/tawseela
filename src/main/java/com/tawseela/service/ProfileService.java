package com.tawseela.service;

import com.tawseela.dto.request.CreateProfileRequest;
import com.tawseela.dto.response.ProfileResponse;
import com.tawseela.dto.request.UpdateProfileRequest;
import com.tawseela.entity.Profile;
import com.tawseela.enums.SystemRole;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProfileService {

    Profile ensureProfile(UUID userId);

    Page<ProfileResponse> list(SystemRole role, Pageable pageable);

    ProfileResponse getById(UUID id);

    ProfileResponse create(CreateProfileRequest request);

    ProfileResponse update(UUID id, UpdateProfileRequest request);
}
