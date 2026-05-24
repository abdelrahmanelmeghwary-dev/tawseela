package com.tawseela.service;

import com.tawseela.entity.Driver;
import java.util.UUID;

/**
 * Ensures a row exists in {@code drivers} (runtime state) for an approved driver user.
 * {@code drivers.id} is always the user's UUID, not {@code driver_profiles.id}.
 */
public interface DriverRuntimeService {

    Driver ensureForUserId(UUID userId);
}
