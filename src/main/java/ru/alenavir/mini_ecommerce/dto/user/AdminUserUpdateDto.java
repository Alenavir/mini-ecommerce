package ru.alenavir.mini_ecommerce.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.alenavir.mini_ecommerce.entity.enums.Role;

import java.util.Set;

@Getter
@Setter
@Schema(description = "DTO для обновления пользователя администратором")
public class AdminUserUpdateDto {

    @Schema(description = "Новое имя пользователя", example = "Ivan Updated")
    @Size(min = 2, max = 255)
    private String name;

    @Schema(description = "Новый email пользователя", example = "new@mail.com")
    @Email
    private String email;

    @Schema(description = "Набор ролей пользователя")
    private Set<Role> roles;

    @Schema(description = "Флаг активности пользователя", example = "true")
    private Boolean isActive;
}