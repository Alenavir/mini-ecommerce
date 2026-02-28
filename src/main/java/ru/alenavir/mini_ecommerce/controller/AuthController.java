package ru.alenavir.mini_ecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.alenavir.mini_ecommerce.dto.user.auth.JwtAuthenticationDto;
import ru.alenavir.mini_ecommerce.dto.user.auth.RefreshTokenDto;
import ru.alenavir.mini_ecommerce.dto.user.auth.UserCredentialsDto;
import ru.alenavir.mini_ecommerce.service.UserService;

import javax.naming.AuthenticationException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Аутентификация и управление JWT токенами")
public class AuthController {

    private final UserService userService;

    @Operation(
            summary = "Вход в систему",
            description = "Аутентификация пользователя по email и паролю. Возвращает access и refresh токены."
    )
    @PostMapping("/sign-in")
    public ResponseEntity<JwtAuthenticationDto> signIn(
            @RequestBody UserCredentialsDto userCredentialsDto
    ) {
        try {
            JwtAuthenticationDto jwtAuthenticationDto = userService.singIn(userCredentialsDto);
            return ResponseEntity.ok(jwtAuthenticationDto);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @Operation(
            summary = "Обновление access токена",
            description = "Получение нового access токена по refresh токену."
    )
    @PostMapping("/refresh")
    public JwtAuthenticationDto refresh(
            @RequestBody RefreshTokenDto refreshTokenDto
    ) throws Exception {
        return userService.refreshToken(refreshTokenDto);
    }
}
