package ru.alenavir.mini_ecommerce.mapper;


import org.mapstruct.Mapper;
import ru.alenavir.mini_ecommerce.dto.UserCreateDto;
import ru.alenavir.mini_ecommerce.dto.UserResponseDto;
import ru.alenavir.mini_ecommerce.entity.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserCreateDto dto);

    UserResponseDto toDto(User user);

    List<UserResponseDto> toDtoList(List<User> users);
}

