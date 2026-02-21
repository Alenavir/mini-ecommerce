package ru.alenavir.mini_ecommerce.dto.user.auth;

import lombok.Data;

@Data
public class JwtAuthenticationDto {
    private String token;
    private String refreshToken;
}
