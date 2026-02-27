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

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService service;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getById(@PathVariable Long id) {
        UserResponseDto response = service.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAll(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name
    ) {
        return ResponseEntity.ok(service.findAll(email, name));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserUpdateDto updateDto
    ) throws BadRequestException {
        UserResponseDto response = service.updateByAdmin(id, updateDto);
        return ResponseEntity.ok(response);
    }

}
