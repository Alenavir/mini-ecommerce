package ru.alenavir.mini_ecommerce.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.alenavir.mini_ecommerce.dto.user.AdminUserUpdateDto;
import ru.alenavir.mini_ecommerce.dto.user.UserCreateDto;
import ru.alenavir.mini_ecommerce.dto.user.UserResponseDto;
import ru.alenavir.mini_ecommerce.dto.user.UserUpdateDto;
import ru.alenavir.mini_ecommerce.security.TokenBlacklistService;
import ru.alenavir.mini_ecommerce.security.jwt.JwtService;
import ru.alenavir.mini_ecommerce.service.UserService;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;
    private final JwtService jwtService;
    private final TokenBlacklistService blacklistService;

    @PostMapping("/registration")
    public ResponseEntity<UserResponseDto> create(
            @Valid @RequestBody UserCreateDto createDto
    ) throws BadRequestException {
        UserResponseDto response = service.save(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            String jti = jwtService.getJtiFromToken(token);
            Date expiration = jwtService.getExpirationFromToken(token);
            blacklistService.blacklistToken(jti, expiration);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id")
    public ResponseEntity<UserResponseDto> getById(@PathVariable Long id) {
        UserResponseDto response = service.findById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id")
    public ResponseEntity<UserResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDto updateDto
    ) throws BadRequestException {
        UserResponseDto response = service.update(id, updateDto);
        return ResponseEntity.ok(response);
    }

}
