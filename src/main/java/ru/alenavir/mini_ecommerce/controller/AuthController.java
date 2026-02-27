package ru.alenavir.mini_ecommerce.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.alenavir.mini_ecommerce.dto.user.auth.JwtAuthenticationDto;
import ru.alenavir.mini_ecommerce.dto.user.auth.RefreshTokenDto;
import ru.alenavir.mini_ecommerce.dto.user.auth.UserCredentialsDto;
import ru.alenavir.mini_ecommerce.security.TokenBlacklistService;
import ru.alenavir.mini_ecommerce.security.jwt.JwtService;
import ru.alenavir.mini_ecommerce.service.UserService;

import javax.naming.AuthenticationException;
import java.util.Date;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserService userService;

    @PostMapping("/sign-in")
    public ResponseEntity<JwtAuthenticationDto> signIn(@RequestBody UserCredentialsDto userCredentialsDto) {
        try {
            JwtAuthenticationDto jwtAuthenticationDto = userService.singIn(userCredentialsDto);
            return ResponseEntity.ok(jwtAuthenticationDto);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @PostMapping("/refresh")
    public JwtAuthenticationDto refresh(@RequestBody RefreshTokenDto refreshTokenDto) throws Exception {
        return userService.refreshToken(refreshTokenDto);
    }
}
