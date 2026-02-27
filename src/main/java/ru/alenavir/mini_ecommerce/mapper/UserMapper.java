package ru.alenavir.mini_ecommerce.mapper;


import org.mapstruct.*;
import ru.alenavir.mini_ecommerce.dto.user.AdminUserUpdateDto;
import ru.alenavir.mini_ecommerce.dto.user.UserCreateDto;
import ru.alenavir.mini_ecommerce.dto.user.UserResponseDto;
import ru.alenavir.mini_ecommerce.dto.user.UserUpdateDto;
import ru.alenavir.mini_ecommerce.entity.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserCreateDto dto);

    UserResponseDto toDto(User user);

    List<UserResponseDto> toDtoList(List<User> users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(UserUpdateDto dto, @MappingTarget User entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDtoByAdmin(AdminUserUpdateDto dto, @MappingTarget User entity);
}

