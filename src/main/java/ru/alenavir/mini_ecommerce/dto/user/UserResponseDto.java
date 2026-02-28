package ru.alenavir.mini_ecommerce.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import ru.alenavir.mini_ecommerce.entity.enums.Role;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Schema(description = "Ответ с информацией о пользователе")
public class UserResponseDto {

    @Schema(description = "ID пользователя", example = "1")
    private Long id;

    @Schema(description = "Имя пользователя", example = "Ivan Petrov")
    private String name;

    @Schema(description = "Email пользователя", example = "ivan@mail.com")
    private String email;

    @Schema(description = "Активен ли пользователь", example = "true")
    private Boolean isActive;

    @Schema(description = "Роли пользователя")
    private Set<Role> roles;

    @Schema(description = "Дата создания пользователя", example = "2026-01-15T10:15:30")
    private LocalDateTime createdAt;
}

