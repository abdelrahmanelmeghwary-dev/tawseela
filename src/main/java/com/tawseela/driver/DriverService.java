package com.tawseela.driver;

import com.tawseela.dto.DriverMeResponse;
import com.tawseela.dto.DriverProfileRequest;
import com.tawseela.entity.DriverProfile;
import com.tawseela.entity.RoleEntity;
import com.tawseela.entity.SystemRole;
import com.tawseela.entity.User;
import com.tawseela.exception.BusinessException;
import com.tawseela.repository.DriverProfileRepository;
import com.tawseela.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverService {

    private final DriverProfileRepository driverProfileRepository;
    private final UserRepository userRepository;

    public DriverService(DriverProfileRepository driverProfileRepository, UserRepository userRepository) {
        this.driverProfileRepository = driverProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public DriverMeResponse me(UUID userId) {
        User user = userRepository
                .findByIdEagerRoles(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));
        DriverProfile dp = driverProfileRepository
                .findByUser_Id(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Driver profile not found"));
        List<String> roles =
                user.getRoles().stream().map(RoleEntity::getName).map(r -> r.name()).collect(Collectors.toList());
        return new DriverMeResponse(
                user.getId(),
                user.getMobileNumber(),
                user.getFirstName(),
                user.getLastName(),
                roles,
                dp.getId(),
                dp.getVehicleType(),
                dp.getVehicleNumber(),
                dp.getLicenseNumber(),
                dp.isApproved());
    }

    @Transactional
    public DriverMeResponse upsertProfile(UUID userId, DriverProfileRequest request) {
        User user = userRepository
                .findByIdEagerRoles(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));
        if (!hasRole(user, SystemRole.DRIVER)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Driver role required");
        }
        DriverProfile dp = driverProfileRepository
                .findByUser_Id(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Driver profile not found"));
        dp.setVehicleType(request.getVehicleType().trim());
        dp.setVehicleNumber(request.getVehicleNumber().trim());
        dp.setLicenseNumber(request.getLicenseNumber().trim());
        driverProfileRepository.save(dp);
        return me(userId);
    }

    private static boolean hasRole(User user, SystemRole role) {
        for (RoleEntity r : user.getRoles()) {
            if (r.getName() == role) {
                return true;
            }
        }
        return false;
    }
}
