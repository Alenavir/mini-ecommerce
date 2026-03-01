package ru.alenavir.mini_ecommerce.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.alenavir.mini_ecommerce.dto.user.UserCreateDto;
import ru.alenavir.mini_ecommerce.dto.user.UserResponseDto;
import ru.alenavir.mini_ecommerce.dto.user.UserUpdateDto;
import ru.alenavir.mini_ecommerce.dto.user.auth.JwtAuthenticationDto;
import ru.alenavir.mini_ecommerce.dto.user.auth.RefreshTokenDto;
import ru.alenavir.mini_ecommerce.dto.user.auth.UserCredentialsDto;
import ru.alenavir.mini_ecommerce.entity.User;
import ru.alenavir.mini_ecommerce.entity.enums.Role;
import ru.alenavir.mini_ecommerce.exceptions.NotFoundException;
import ru.alenavir.mini_ecommerce.mapper.UserMapper;
import ru.alenavir.mini_ecommerce.repo.UserRepo;
import ru.alenavir.mini_ecommerce.security.jwt.JwtService;

import javax.naming.AuthenticationException;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepo repo;

    @Mock
    private UserMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private MeterRegistry meterRegistry;

    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();

        userService = new UserService(
                repo,
                mapper,
                passwordEncoder,
                jwtService,
                meterRegistry
        );

        user = new User();
        user.setId(1L);
        user.setEmail("test@mail.com");
        user.setPasswordHash("encoded");
        user.setRoles(Set.of(Role.USER));
    }

    @Test
    void save_shouldCreateUserSuccessfully() {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail("test@mail.com");
        dto.setPassword("123");

        when(mapper.toEntity(dto)).thenReturn(new User());
        when(passwordEncoder.encode("123")).thenReturn("encoded");
        when(repo.save(any(User.class))).thenReturn(user);
        when(mapper.toDto(user)).thenReturn(new UserResponseDto());

        UserResponseDto result = userService.save(dto);

        assertNotNull(result);
        verify(repo).save(any(User.class));
    }

    @Test
    void findById_shouldReturnUser() {
        when(repo.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.toDto(user)).thenReturn(new UserResponseDto());

        UserResponseDto result = userService.findById(1L);

        assertNotNull(result);
        verify(repo).findById(1L);
    }

    @Test
    void findById_shouldThrowException_whenNotFound() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.findById(1L));
    }

    @Test
    void delete_shouldDeleteUser() {
        when(repo.existsById(1L)).thenReturn(true);

        userService.delete(1L);

        verify(repo).deleteById(1L);
    }

    @Test
    void delete_shouldThrowException_whenNotFound() {
        when(repo.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> userService.delete(1L));
    }

    @Test
    void update_shouldUpdateUserSuccessfully() throws Exception {
        UserUpdateDto dto = new UserUpdateDto();

        when(repo.findById(1L)).thenReturn(Optional.of(user));
        when(repo.save(user)).thenReturn(user);
        when(mapper.toDto(user)).thenReturn(new UserResponseDto());

        UserResponseDto result = userService.update(1L, dto);

        assertNotNull(result);
        verify(repo).save(user);
    }

    @Test
    void signIn_shouldReturnToken_whenCredentialsValid() throws Exception {
        UserCredentialsDto dto = new UserCredentialsDto();
        dto.setEmail("test@mail.com");
        dto.setPassword("123");

        when(repo.findByEmail("test@mail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123", "encoded")).thenReturn(true);
        when(jwtService.generateAuthToken("test@mail.com"))
                .thenReturn(new JwtAuthenticationDto());

        JwtAuthenticationDto result = userService.singIn(dto);

        assertNotNull(result);
        verify(jwtService).generateAuthToken("test@mail.com");
    }

    @Test
    void signIn_shouldThrowException_whenInvalidCredentials() {
        UserCredentialsDto dto = new UserCredentialsDto();
        dto.setEmail("wrong@mail.com");
        dto.setPassword("123");

        when(repo.findByEmail("wrong@mail.com"))
                .thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class,
                () -> userService.singIn(dto));
    }

    @Test
    void refreshToken_shouldReturnNewToken_whenValid() throws Exception {
        RefreshTokenDto dto = new RefreshTokenDto();
        dto.setRefreshToken("refresh");

        when(jwtService.validateJwtToken("refresh")).thenReturn(true);
        when(jwtService.getEmailFromToken("refresh"))
                .thenReturn("test@mail.com");
        when(repo.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(user));
        when(jwtService.refreshBaseToken("test@mail.com", "refresh"))
                .thenReturn(new JwtAuthenticationDto());

        JwtAuthenticationDto result = userService.refreshToken(dto);

        assertNotNull(result);
        verify(jwtService).refreshBaseToken("test@mail.com", "refresh");
    }

    @Test
    void refreshToken_shouldThrowException_whenInvalid() {
        RefreshTokenDto dto = new RefreshTokenDto();
        dto.setRefreshToken("bad");

        when(jwtService.validateJwtToken("bad")).thenReturn(false);

        assertThrows(AuthenticationException.class,
                () -> userService.refreshToken(dto));
    }
}