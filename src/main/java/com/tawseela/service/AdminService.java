package com.tawseela.service;

import com.tawseela.dto.response.AdminDriverRowDto;
import com.tawseela.dto.response.AdminUserRowDto;
import java.util.List;
import java.util.UUID;

public interface AdminService {

    List<AdminUserRowDto> listUsers();

    List<AdminDriverRowDto> listDrivers();

    AdminDriverRowDto approveDriver(UUID driverProfileId);

    AdminDriverRowDto rejectDriver(UUID driverProfileId);
}
