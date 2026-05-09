package com.tawseela.mapper;

import com.tawseela.dto.AuthTokensResponse.UserInfoDto;
import com.tawseela.entity.RoleEntity;
import com.tawseela.entity.User;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserInfoDto toUserInfo(User user) {
        return new UserInfoDto(
                user.getId(),
                user.getMobileNumber(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toList()));
    }
}
