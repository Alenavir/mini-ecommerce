package ru.alenavir.mini_ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo repo;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserResponseDto save(UserCreateDto dto) {

        User user = mapper.toEntity(dto);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRoles(Set.of(Role.USER));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setIsActive(true);

        return mapper.toDto(repo.save(user));
    }

    public List<UserResponseDto> findAll(String email, String name) {
        return mapper.toDtoList(repo.search(email, name));
    }

    @Cacheable(value = "users", key = "#id")
    public UserResponseDto findById(Long id) {
        User user = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
        return mapper.toDto(user);
    }

    @CacheEvict(value = "users", key = "#id")
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException("User with id " + id + " not found");
        }

        repo.deleteById(id);
    }

    @CachePut(value = "users", key = "#id")
    public UserResponseDto deactivate(Long id) {
        User user = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));

        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());

        return mapper.toDto(repo.save(user));
    }

    @CachePut(value = "users", key = "#id")
    public UserResponseDto update(Long id, UserUpdateDto dto) throws BadRequestException {
        User exist = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));

        mapper.updateUserFromDto(dto, exist);

        exist.setUpdatedAt(LocalDateTime.now());

        return mapper.toDto(repo.save(exist));
    }

    @CachePut(value = "users", key = "#id")
    public UserResponseDto updateByAdmin(Long id, AdminUserUpdateDto dto) throws BadRequestException {
        User exist = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));

        mapper.updateUserFromDtoByAdmin(dto, exist);

        exist.setUpdatedAt(LocalDateTime.now());

        return mapper.toDto(repo.save(exist));
    }

    public JwtAuthenticationDto singIn(UserCredentialsDto userCredentialsDto) throws AuthenticationException {
        User user = findByCredentials(userCredentialsDto);
        return jwtService.generateAuthToken(user.getEmail());
    }

    public JwtAuthenticationDto refreshToken(RefreshTokenDto refreshTokenDto) throws Exception {
        String refreshToken = refreshTokenDto.getRefreshToken();
        if (refreshToken != null && jwtService.validateJwtToken(refreshToken)) {
            User user = findByEmail(jwtService.getEmailFromToken(refreshToken));
            return jwtService.refreshBaseToken(user.getEmail(), refreshToken);
        }
        throw new AuthenticationException("Invalid refresh token");
    }

    private User findByCredentials(UserCredentialsDto userCredentialsDto) throws AuthenticationException {
        return repo.findByEmail(userCredentialsDto.getEmail())
                .filter(user -> passwordEncoder.matches(userCredentialsDto.getPassword(), user.getPasswordHash()))
                .orElseThrow(() -> new AuthenticationException("Email or password is not correct"));
    }

    private User findByEmail(String email) {
        return repo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User with email " + email + " not found"));
    }
}

