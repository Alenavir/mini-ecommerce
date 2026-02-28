package ru.alenavir.mini_ecommerce.dto.user.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Ответ при успешной аутентификации")
public class JwtAuthenticationDto {

    @Schema(description = "JWT access токен", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Refresh токен", example = "d9f3b6c2-4f10-4e8a-9c6f-123456789abc")
    private String refreshToken;
}
