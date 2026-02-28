package ru.alenavir.mini_ecommerce.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO для регистрации нового пользователя")
public class UserCreateDto {

    @Schema(description = "Имя пользователя", example = "Ivan Petrov")
    @NotBlank
    @Size(min = 2, max = 255)
    private String name;

    @Schema(description = "Email пользователя", example = "ivan@mail.com")
    @NotBlank
    @Email
    private String email;

    @Schema(description = "Пароль пользователя (минимум одна буква и одна цифра)", example = "password123")
    @NotBlank
    @Size(min = 6, max = 100)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "Password must contain at least one letter and one number")
    private String password;
}
