package com.tawseela.mapper;

import com.tawseela.dto.response.DriverProfileSummary;
import com.tawseela.dto.response.DriverResponse;
import com.tawseela.entity.Driver;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DriverMapper {

    @Mapping(target = "id", source = "driver.id")
    @Mapping(target = "online", source = "driver.online")
    @Mapping(target = "lastSeen", source = "driver.lastSeen")
    @Mapping(target = "currentLat", source = "driver.currentLat")
    @Mapping(target = "currentLng", source = "driver.currentLng")
    @Mapping(target = "totalDeliveries", source = "driver.totalDeliveries")
    @Mapping(target = "createdAt", source = "driver.createdAt")
    @Mapping(target = "updatedAt", source = "driver.updatedAt")
    @Mapping(target = "profile", source = "profile")
    DriverResponse toResponse(Driver driver, DriverProfileSummary profile);
}
