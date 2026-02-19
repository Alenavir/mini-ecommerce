package ru.alenavir.mini_ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.alenavir.mini_ecommerce.dto.UserCreateDto;
import ru.alenavir.mini_ecommerce.dto.UserResponseDto;
import ru.alenavir.mini_ecommerce.dto.UserUpdateDto;
import ru.alenavir.mini_ecommerce.entity.User;
import ru.alenavir.mini_ecommerce.exceptions.NotFoundException;
import ru.alenavir.mini_ecommerce.mapper.UserMapper;
import ru.alenavir.mini_ecommerce.repo.UserRepo;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo repo;
    private final UserMapper mapper;

    public UserResponseDto save(UserCreateDto dto) {

        User user = mapper.toEntity(dto);
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
                .orElseThrow(() ->
                        new NotFoundException("User with id " + id + " not found")
                );
        return mapper.toDto(user);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public UserResponseDto update(Long id, UserUpdateDto dto) {

        User exist = repo.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("User with id " + id + " not found")
                );

        if (dto.getName() != null)
            exist.setName(dto.getName());

        if (dto.getEmail() != null)
            exist.setEmail(dto.getEmail());

        if (dto.getIsActive() != null)
            exist.setIsActive(dto.getIsActive());

        exist.setUpdatedAt(LocalDateTime.now());

        return mapper.toDto(repo.save(exist));
    }
}

