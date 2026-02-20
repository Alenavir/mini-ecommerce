package ru.alenavir.mini_ecommerce.mapper;


import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.alenavir.mini_ecommerce.dto.user.UserCreateDto;
import ru.alenavir.mini_ecommerce.dto.user.UserResponseDto;
import ru.alenavir.mini_ecommerce.dto.user.UserUpdateDto;
import ru.alenavir.mini_ecommerce.entity.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserCreateDto dto);

    UserResponseDto toDto(User user);

    List<UserResponseDto> toDtoList(List<User> users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(UserUpdateDto dto, @MappingTarget User entity);
}

