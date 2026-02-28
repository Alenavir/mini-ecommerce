package ru.alenavir.mini_ecommerce.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.alenavir.mini_ecommerce.dto.user.AdminUserUpdateDto;
import ru.alenavir.mini_ecommerce.dto.user.UserResponseDto;
import ru.alenavir.mini_ecommerce.service.UserService;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(
        name = "Admin Users",
        description = "Административное управление пользователями"
)
public class AdminUserController {

    private final UserService service;

    @Operation(summary = "Получить пользователя по ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getById(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long id) {

        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Получить список пользователей (с фильтрацией)")
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAll(

            @Parameter(description = "Фильтр по email", example = "admin@mail.com")
            @RequestParam(required = false) String email,

            @Parameter(description = "Фильтр по имени", example = "Ivan")
            @RequestParam(required = false) String name
    ) {
        return ResponseEntity.ok(service.findAll(email, name));
    }

    @Operation(summary = "Деактивировать пользователя")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long id) {

        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Обновить пользователя (администратор)")
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDto> update(

            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для обновления пользователя",
                    required = true)
            @Valid @RequestBody AdminUserUpdateDto updateDto
    ) throws BadRequestException {

        return ResponseEntity.ok(service.updateByAdmin(id, updateDto));
    }
}
