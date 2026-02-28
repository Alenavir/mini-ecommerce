package ru.alenavir.mini_ecommerce.dto.user.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO для входа пользователя")
public class UserCredentialsDto {

    @Schema(description = "Email пользователя", example = "admin@mail.com")
    private String email;

    @Schema(description = "Пароль пользователя", example = "password123")
    private String password;
}
