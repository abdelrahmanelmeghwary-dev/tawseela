package com.tawseela.service.impl;

import com.tawseela.dto.response.AdminDriverRowDto;
import com.tawseela.dto.response.AdminUserRowDto;
import com.tawseela.entity.DriverProfile;
import com.tawseela.entity.RoleEntity;
import com.tawseela.entity.User;
import com.tawseela.exception.BusinessException;
import com.tawseela.repository.DriverProfileRepository;
import com.tawseela.repository.UserRepository;
import com.tawseela.service.AdminService;
import com.tawseela.service.DriverRuntimeService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final DriverRuntimeService driverRuntimeService;

    public AdminServiceImpl(
            UserRepository userRepository,
            DriverProfileRepository driverProfileRepository,
            DriverRuntimeService driverRuntimeService) {
        this.userRepository = userRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.driverRuntimeService = driverRuntimeService;
    }

    @Transactional(readOnly = true)
    public List<AdminUserRowDto> listUsers() {
        return userRepository.findAllWithRoles().stream().map(this::toUserRow).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdminDriverRowDto> listDrivers() {
        return driverProfileRepository.findAllWithUser().stream().map(this::toDriverRow).collect(Collectors.toList());
    }

    @Transactional
    public AdminDriverRowDto approveDriver(UUID driverProfileId) {
        DriverProfile dp = driverProfileRepository
                .findByIdWithUser(driverProfileId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Driver profile not found"));
        dp.setApproved(true);
        User user = dp.getUser();
        user.setEnabled(true);
        driverProfileRepository.save(dp);
        userRepository.save(user);
        driverRuntimeService.ensureForUserId(user.getId());
        return toDriverRow(dp);
    }

    @Transactional
    public AdminDriverRowDto rejectDriver(UUID driverProfileId) {
        DriverProfile dp = driverProfileRepository
                .findByIdWithUser(driverProfileId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Driver profile not found"));
        dp.setApproved(false);
        User user = dp.getUser();
        user.setEnabled(false);
        driverProfileRepository.save(dp);
        userRepository.save(user);
        return toDriverRow(dp);
    }

    private AdminUserRowDto toUserRow(User u) {
        List<String> roles =
                u.getRoles().stream().map(RoleEntity::getName).map(r -> r.name()).collect(Collectors.toList());
        return new AdminUserRowDto(
                u.getId(),
                u.getMobileNumber(),
                u.getFirstName(),
                u.getLastName(),
                u.isEnabled(),
                u.isPhoneVerified(),
                roles,
                u.getCreatedAt());
    }

    private AdminDriverRowDto toDriverRow(DriverProfile dp) {
        User u = dp.getUser();
        List<String> roles =
                u.getRoles().stream().map(RoleEntity::getName).map(r -> r.name()).collect(Collectors.toList());
        return new AdminDriverRowDto(
                dp.getId(),
                u.getId(),
                u.getMobileNumber(),
                u.getFirstName(),
                u.getLastName(),
                u.isEnabled(),
                u.isPhoneVerified(),
                roles,
                dp.getVehicleType(),
                dp.getVehicleNumber(),
                dp.getLicenseNumber(),
                dp.isApproved(),
                dp.getCreatedAt());
    }
}
