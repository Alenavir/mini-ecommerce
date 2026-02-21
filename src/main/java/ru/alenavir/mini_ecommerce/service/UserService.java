package ru.alenavir.mini_ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import ru.alenavir.mini_ecommerce.dto.user.UserCreateDto;
import ru.alenavir.mini_ecommerce.dto.user.UserResponseDto;
import ru.alenavir.mini_ecommerce.dto.user.UserUpdateDto;
import ru.alenavir.mini_ecommerce.entity.User;
import ru.alenavir.mini_ecommerce.entity.enums.Role;
import ru.alenavir.mini_ecommerce.exceptions.NotFoundException;
import ru.alenavir.mini_ecommerce.mapper.UserMapper;
import ru.alenavir.mini_ecommerce.repo.UserRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo repo;
    private final UserMapper mapper;

    public UserResponseDto save(UserCreateDto dto) {

        User user = mapper.toEntity(dto);
        user.setPasswordHash(dto.getPassword());
        user.setRoles(Set.of(Role.USER));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setIsActive(true);

        return mapper.toDto(repo.save(user));
    }

    public List<UserResponseDto> findAll() {
        return mapper.toDtoList(repo.findAll());
    }

    public UserResponseDto findById(Long id) {
        User user = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
        return mapper.toDto(user);
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException("User with id " + id + " not found");
        }

        repo.deleteById(id);
    }

    public UserResponseDto update(Long id, UserUpdateDto dto) throws BadRequestException {
        User exist = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));

        mapper.updateUserFromDto(dto, exist);

        exist.setUpdatedAt(LocalDateTime.now());

        return mapper.toDto(repo.save(exist));
    }
}

