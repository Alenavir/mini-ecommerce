package ru.alenavir.mini_ecommerce.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.alenavir.mini_ecommerce.dto.user.AdminUserUpdateDto;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo repo;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MeterRegistry meterRegistry;

    public UserResponseDto save(UserCreateDto dto) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            log.info("Создание пользователя: email={}", dto.getEmail());

            User user = mapper.toEntity(dto);
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
            user.setRoles(Set.of(Role.USER));
            LocalDateTime now = LocalDateTime.now();
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
            user.setIsActive(true);

            User saved = repo.save(user);
            log.info("Пользователь создан: userId={}, email={}", saved.getId(), saved.getEmail());
            meterRegistry.counter("users.created").increment();

            return mapper.toDto(saved);
        } finally {
            timer.stop(meterRegistry.timer("users.save.time"));
        }
    }

    public List<UserResponseDto> findAll(String email, String name) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            List<User> users = repo.search(email, name);
            log.info("Поиск пользователей выполнен: count={}", users.size());
            return mapper.toDtoList(users);
        } finally {
            timer.stop(meterRegistry.timer("users.findAll.time"));
        }
    }

    @Cacheable(value = "users", key = "#id")
    public UserResponseDto findById(Long id) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            User user = repo.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Пользователь с id={} не найден", id);
                        return new NotFoundException("User with id " + id + " not found");
                    });

            log.info("Пользователь найден: userId={}", id);
            return mapper.toDto(user);
        } finally {
            timer.stop(meterRegistry.timer("users.findById.time"));
        }
    }

    @CacheEvict(value = "users", key = "#id")
    public void delete(Long id) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            if (!repo.existsById(id)) {
                log.warn("Попытка удалить несуществующего пользователя: userId={}", id);
                throw new NotFoundException("User with id " + id + " not found");
            }

            repo.deleteById(id);
            log.info("Пользователь удален: userId={}", id);
            meterRegistry.counter("users.deleted").increment();
        } finally {
            timer.stop(meterRegistry.timer("users.delete.time"));
        }
    }

    @CachePut(value = "users", key = "#id")
    public UserResponseDto deactivate(Long id) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            User user = repo.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Попытка деактивировать несуществующего пользователя: userId={}", id);
                        return new NotFoundException("User with id " + id + " not found");
                    });

            user.setIsActive(false);
            user.setUpdatedAt(LocalDateTime.now());

            User saved = repo.save(user);
            log.info("Пользователь деактивирован: userId={}", id);
            meterRegistry.counter("users.deactivated").increment();

            return mapper.toDto(saved);
        } finally {
            timer.stop(meterRegistry.timer("users.deactivate.time"));
        }
    }

    @CachePut(value = "users", key = "#id")
    public UserResponseDto update(Long id, UserUpdateDto dto) throws BadRequestException {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            User exist = repo.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Попытка обновить несуществующего пользователя: userId={}", id);
                        return new NotFoundException("User with id " + id + " not found");
                    });

            mapper.updateUserFromDto(dto, exist);
            exist.setUpdatedAt(LocalDateTime.now());

            User saved = repo.save(exist);
            log.info("Пользователь обновлен: userId={}", id);
            meterRegistry.counter("users.updated").increment();

            return mapper.toDto(saved);
        } finally {
            timer.stop(meterRegistry.timer("users.update.time"));
        }
    }

    @CachePut(value = "users", key = "#id")
    public UserResponseDto updateByAdmin(Long id, AdminUserUpdateDto dto) throws BadRequestException {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            User exist = repo.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Попытка админского обновления несуществующего пользователя: userId={}", id);
                        return new NotFoundException("User with id " + id + " not found");
                    });

            mapper.updateUserFromDtoByAdmin(dto, exist);
            exist.setUpdatedAt(LocalDateTime.now());

            User saved = repo.save(exist);
            log.info("Пользователь обновлен админом: userId={}", id);
            meterRegistry.counter("users.updatedByAdmin").increment();

            return mapper.toDto(saved);
        } finally {
            timer.stop(meterRegistry.timer("users.updateByAdmin.time"));
        }
    }

    public JwtAuthenticationDto singIn(UserCredentialsDto userCredentialsDto) throws AuthenticationException {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            User user = findByCredentials(userCredentialsDto);
            log.info("Пользователь вошел в систему: email={}", user.getEmail());
            meterRegistry.counter("users.signIn.success").increment();
            return jwtService.generateAuthToken(user.getEmail());
        } finally {
            timer.stop(meterRegistry.timer("users.signIn.time"));
        }
    }

    public JwtAuthenticationDto refreshToken(RefreshTokenDto refreshTokenDto) throws Exception {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            String refreshToken = refreshTokenDto.getRefreshToken();
            if (refreshToken != null && jwtService.validateJwtToken(refreshToken)) {
                User user = findByEmail(jwtService.getEmailFromToken(refreshToken));
                log.info("Токен обновлен для пользователя: email={}", user.getEmail());
                meterRegistry.counter("users.refreshToken.success").increment();
                return jwtService.refreshBaseToken(user.getEmail(), refreshToken);
            }
            log.warn("Не удалось обновить токен: invalid refresh token");
            meterRegistry.counter("users.refreshToken.failed").increment();
            throw new AuthenticationException("Invalid refresh token");
        } finally {
            timer.stop(meterRegistry.timer("users.refreshToken.time"));
        }
    }

    private User findByCredentials(UserCredentialsDto userCredentialsDto) throws AuthenticationException {
        return repo.findByEmail(userCredentialsDto.getEmail())
                .filter(user -> passwordEncoder.matches(userCredentialsDto.getPassword(), user.getPasswordHash()))
                .orElseThrow(() -> {
                    log.warn("Неудачная попытка входа: email={}", userCredentialsDto.getEmail());
                    meterRegistry.counter("users.signIn.failed").increment();
                    return new AuthenticationException("Email or password is not correct");
                });
    }

    private User findByEmail(String email) {
        return repo.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Пользователь с email={} не найден", email);
                    return new NotFoundException("User with email " + email + " not found");
                });
    }
}

