package ru.alenavir.mini_ecommerce.controller;

import com.sun.security.auth.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.scheduling.ScheduledTasksEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.alenavir.mini_ecommerce.dto.order.OrderCreateDto;
import ru.alenavir.mini_ecommerce.dto.order.OrderResponseDto;
import ru.alenavir.mini_ecommerce.dto.order.OrderUpdateDto;
import ru.alenavir.mini_ecommerce.entity.enums.OrderStatus;
import ru.alenavir.mini_ecommerce.service.OrderService;
import ru.alenavir.mini_ecommerce.service.UserService;

import java.util.List;
import java.util.logging.Logger;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Управление заказами")
public class OrderController {

    private final OrderService service;
    private final UserService userService;

    @Operation(
            summary = "Создать заказ",
            description = "Создание нового заказа для авторизованного пользователя"
    )
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponseDto> create(
            @RequestBody @Valid OrderCreateDto createDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(createDto));
    }

    @Operation(summary = "Получить последние заказы пользователя")
    @GetMapping("/last")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderResponseDto>> getLastOrders(
            Authentication authentication
    ) {
        String email = authentication.getName();
        Long userId = userService.findByEmail(email).getId();
        log.info("Пользователь last: userId={}", userId);
        List<OrderResponseDto> lastOrders = service.findLastOrders(userId);
        return ResponseEntity.ok(lastOrders);
    }

    @Operation(summary = "Получить заказ по ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOwner(#id, authentication.principal.id)")
    public ResponseEntity<OrderResponseDto> getById(
            @Parameter(description = "ID заказа", example = "1")
            @PathVariable Long id) {

        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Получить список всех заказов (только ADMIN)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponseDto>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @Operation(summary = "Удалить заказ (только ADMIN)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID заказа", example = "1")
            @PathVariable Long id) {

        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Обновить данные заказа (только ADMIN)")
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponseDto> update(
            @Parameter(description = "ID заказа", example = "1")
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestBody @Valid OrderUpdateDto updateDto
    ) {
        return ResponseEntity.ok(service.update(id, updateDto));
    }

    @Operation(summary = "Изменить статус заказа (только ADMIN)")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponseDto> changeStatus(
            @Parameter(description = "ID заказа", example = "1")
            @PathVariable("id") Long orderId,

            @Parameter(description = "Новый статус заказа", example = "PAID")
            @RequestParam OrderStatus status
    ) {
        return ResponseEntity.ok(service.changeStatus(orderId, status));
    }
}
