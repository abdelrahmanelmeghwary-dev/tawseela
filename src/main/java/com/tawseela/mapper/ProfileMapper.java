package com.tawseela.mapper;

import com.tawseela.dto.response.ProfileResponse;
import com.tawseela.entity.Profile;
import com.tawseela.util.RoleNames;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = RoleNames.class)
public interface ProfileMapper {

    @Mapping(target = "roles", expression = "java(RoleNames.fromUser(profile.getUser()))")
    ProfileResponse toResponse(Profile profile);
}
