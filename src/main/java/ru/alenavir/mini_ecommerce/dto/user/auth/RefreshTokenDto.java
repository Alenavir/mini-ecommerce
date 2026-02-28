package ru.alenavir.mini_ecommerce.dto.user.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO для обновления access токена")
public class RefreshTokenDto {

    @Schema(description = "Refresh токен", example = "d9f3b6c2-4f10-4e8a-9c6f-123456789abc")
    private String refreshToken;
}

