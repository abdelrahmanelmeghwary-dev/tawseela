package com.tawseela.service.impl;

import com.tawseela.dto.response.DriverResponse;
import com.tawseela.dto.request.UpdateDriverRequest;
import com.tawseela.entity.Driver;
import com.tawseela.mapper.DriverMapper;
import com.tawseela.dto.response.DriverProfileSummary;
import com.tawseela.entity.Profile;
import com.tawseela.repository.DriverRepository;
import com.tawseela.repository.ProfileRepository;
import com.tawseela.service.DriverService;
import com.tawseela.specification.DriverSpecifications;
import com.tawseela.enums.SystemRole;
import com.tawseela.entity.User;
import com.tawseela.exception.BusinessException;
import com.tawseela.exception.ResourceNotFoundException;
import com.tawseela.exception.UnauthorizedActionException;
import com.tawseela.repository.UserRepository;
import com.tawseela.security.SecurityUtils;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverServiceImpl implements DriverService {

    private static final Logger log = LoggerFactory.getLogger(DriverServiceImpl.class);

    private final DriverRepository driverRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final DriverMapper driverMapper;

    public DriverServiceImpl(
            DriverRepository driverRepository,
            ProfileRepository profileRepository,
            UserRepository userRepository,
            DriverMapper driverMapper) {
        this.driverRepository = driverRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.driverMapper = driverMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DriverResponse> list(Boolean online, Pageable pageable) {
        if (!SecurityUtils.hasRole(SystemRole.CUSTOMER)
                && !SecurityUtils.hasRole(SystemRole.DRIVER)
                && !SecurityUtils.isAdmin()) {
            throw new UnauthorizedActionException("Access denied");
        }
        Specification<Driver> spec = DriverSpecifications.isOnline(online);
        return driverRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DriverResponse getById(UUID id) {
        return toResponse(loadDriver(id));
    }

    @Override
    @Transactional
    public DriverResponse create() {
        if (!SecurityUtils.hasRole(SystemRole.DRIVER)) {
            throw new UnauthorizedActionException("Only drivers can register driver runtime profile");
        }
        UUID userId = SecurityUtils.requireUserId();
        if (driverRepository.existsById(userId)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Driver profile already exists");
        }
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Driver driver = new Driver();
        driver.setId(userId);
        driver.setUser(user);
        driver.setOnline(false);
        driver.setTotalDeliveries(0);
        driver = driverRepository.save(driver);
        log.info("Driver runtime profile created userId={}", userId);
        return toResponse(driver);
    }

    @Override
    @Transactional
    public DriverResponse update(UUID id, UpdateDriverRequest request) {
        if (!id.equals(SecurityUtils.requireUserId())) {
            throw new UnauthorizedActionException("You can only update your own driver profile");
        }
        Driver driver = loadDriver(id);
        if (request.getOnline() != null) {
            driver.setOnline(request.getOnline());
        }
        if (request.getLastSeen() != null) {
            driver.setLastSeen(request.getLastSeen());
        }
        if (request.getCurrentLat() != null) {
            driver.setCurrentLat(request.getCurrentLat());
        }
        if (request.getCurrentLng() != null) {
            driver.setCurrentLng(request.getCurrentLng());
        }
        driver = driverRepository.save(driver);
        return toResponse(driver);
    }

    private DriverResponse toResponse(Driver driver) {
        DriverProfileSummary summary = profileRepository
                .findById(driver.getId())
                .map(this::toSummary)
                .orElse(DriverProfileSummary.builder().id(driver.getId()).build());
        return driverMapper.toResponse(driver, summary);
    }

    private DriverProfileSummary toSummary(Profile profile) {
        return DriverProfileSummary.builder()
                .id(profile.getId())
                .fullName(profile.getFullName())
                .phone(profile.getPhone())
                .build();
    }

    private Driver loadDriver(UUID id) {
        return driverRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
    }
}
