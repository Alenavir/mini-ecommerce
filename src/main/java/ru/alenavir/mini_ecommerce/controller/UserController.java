package ru.alenavir.mini_ecommerce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.alenavir.mini_ecommerce.dto.user.UserCreateDto;
import ru.alenavir.mini_ecommerce.dto.user.UserResponseDto;
import ru.alenavir.mini_ecommerce.dto.user.UserUpdateDto;
import ru.alenavir.mini_ecommerce.security.TokenBlacklistService;
import ru.alenavir.mini_ecommerce.security.jwt.JwtService;
import ru.alenavir.mini_ecommerce.service.UserService;

import java.util.Date;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Управление пользователями")
public class UserController {

    private final UserService service;
    private final JwtService jwtService;
    private final TokenBlacklistService blacklistService;

    @Operation(summary = "Регистрация нового пользователя")
    @PostMapping("/registration")
    public ResponseEntity<UserResponseDto> create(
            @RequestBody @Valid UserCreateDto createDto
    ) throws BadRequestException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.save(createDto));
    }

    @Operation(summary = "Выход из системы (logout)")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Parameter(description = "Authorization Header с Bearer токеном")
            HttpServletRequest request
    ) {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            String jti = jwtService.getJtiFromToken(token);
            Date expiration = jwtService.getExpirationFromToken(token);
            blacklistService.blacklistToken(jti, expiration);
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получить данные пользователя по ID")
    @GetMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id")
    public ResponseEntity<UserResponseDto> getById(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Удалить пользователя")
    @DeleteMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long id
    ) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Обновить данные пользователя")
    @PatchMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id")
    public ResponseEntity<UserResponseDto> update(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestBody @Valid UserUpdateDto updateDto
    ) throws BadRequestException {
        return ResponseEntity.ok(service.update(id, updateDto));
    }
}
