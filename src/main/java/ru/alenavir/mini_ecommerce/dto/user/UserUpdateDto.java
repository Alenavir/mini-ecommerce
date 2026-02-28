package ru.alenavir.mini_ecommerce.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO для обновления данных пользователя")
public class UserUpdateDto {

    @Schema(description = "Новое имя пользователя", example = "Ivan Updated")
    @Size(min = 2, max = 255)
    private String name;

    @Schema(description = "Новый email пользователя", example = "new@mail.com")
    @Email
    private String email;
}
