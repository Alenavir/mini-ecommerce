package ru.alenavir.mini_ecommerce.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.alenavir.mini_ecommerce.dto.order.OrderCreateDto;
import ru.alenavir.mini_ecommerce.dto.order.OrderResponseDto;
import ru.alenavir.mini_ecommerce.dto.order.OrderUpdateDto;
import ru.alenavir.mini_ecommerce.entity.enums.OrderStatus;
import ru.alenavir.mini_ecommerce.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponseDto> create(
            @RequestBody @Valid OrderCreateDto createDto
    ) {
        OrderResponseDto response = service.create(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOwner(#id, authentication.principal.id)")
    public ResponseEntity<OrderResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponseDto>> getAll() {
        List<OrderResponseDto> response = service.findAll();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponseDto> update(
            @PathVariable Long id,
            @RequestBody @Valid OrderUpdateDto updateDto
    ) {
        OrderResponseDto response = service.update(id, updateDto);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponseDto> changeStatus(
            @PathVariable("id") Long orderId,
            @RequestParam OrderStatus status
    ) {
        OrderResponseDto response = service.changeStatus(orderId, status);
        return ResponseEntity.ok(response);
    }
}
