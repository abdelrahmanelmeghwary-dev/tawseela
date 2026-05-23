package com.tawseela.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tawseela.dto.response.NotificationResponse;
import com.tawseela.entity.Notification;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    ObjectMapper JSON = new ObjectMapper();

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "read", source = "read")
    @Mapping(target = "data", expression = "java(parseData(notification.getData()))")
    NotificationResponse toResponse(Notification notification);

    default Map<String, String> parseData(String json) {
        try {
            if (json == null || json.isBlank()) {
                return Map.of();
            }
            return JSON.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }
}
