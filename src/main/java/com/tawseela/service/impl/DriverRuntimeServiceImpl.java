package com.tawseela.service.impl;

import com.tawseela.entity.Driver;
import com.tawseela.entity.DriverProfile;
import com.tawseela.entity.User;
import com.tawseela.exception.ResourceNotFoundException;
import com.tawseela.repository.DriverProfileRepository;
import com.tawseela.repository.DriverRepository;
import com.tawseela.repository.UserRepository;
import com.tawseela.service.DriverRuntimeService;
import com.tawseela.service.ProfileService;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverRuntimeServiceImpl implements DriverRuntimeService {

    private static final Logger log = LoggerFactory.getLogger(DriverRuntimeServiceImpl.class);

    private final DriverRepository driverRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final UserRepository userRepository;
    private final ProfileService profileService;

    public DriverRuntimeServiceImpl(
            DriverRepository driverRepository,
            DriverProfileRepository driverProfileRepository,
            UserRepository userRepository,
            ProfileService profileService) {
        this.driverRepository = driverRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.userRepository = userRepository;
        this.profileService = profileService;
    }

    @Override
    @Transactional
    public Driver ensureForUserId(UUID userId) {
        return driverRepository.findById(userId).orElseGet(() -> provisionApprovedDriver(userId));
    }

    private Driver provisionApprovedDriver(UUID userId) {
        DriverProfile driverProfile = driverProfileRepository
                .findByUser_Id(userId)
                .filter(DriverProfile::isApproved)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Driver not found. Use the user id (not driverProfileId). "
                                + "The driver must be approved and have a runtime profile."));
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Legacy DBs may reference profiles(id) from drivers(id); ensure delivery profile exists.
        profileService.ensureProfile(userId);

        Driver driver = new Driver();
        driver.setId(userId);
        driver.setUser(user);
        driver.setOnline(false);
        driver.setTotalDeliveries(0);
        driver = saveOrGetExisting(driver);
        log.info("Provisioned runtime driver row userId={} driverProfileId={}", userId, driverProfile.getId());
        return driver;
    }

    private Driver saveOrGetExisting(Driver driver) {
        try {
            return driverRepository.save(driver);
        } catch (DataIntegrityViolationException ex) {
            return driverRepository
                    .findById(driver.getId())
                    .orElseThrow(() -> ex);
        }
    }
}
