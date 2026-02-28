package ru.alenavir.mini_ecommerce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.alenavir.mini_ecommerce.dto.product.ProductCreateDto;
import ru.alenavir.mini_ecommerce.dto.product.ProductResponseDto;
import ru.alenavir.mini_ecommerce.dto.product.ProductSearchDto;
import ru.alenavir.mini_ecommerce.dto.product.ProductUpdateDto;
import ru.alenavir.mini_ecommerce.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Управление товарами")
public class ProductController {

    private final ProductService service;

    @Operation(summary = "Создать новый продукт (только ADMIN)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDto> create(
            @org.springframework.web.bind.annotation.RequestBody @Valid ProductCreateDto createDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(createDto));
    }

    @Operation(summary = "Получить продукт по ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getById(
            @Parameter(description = "ID продукта", example = "1")
            @PathVariable Long id) {

        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Поиск продуктов с фильтрацией")
    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> search(
            @Parameter(description = "Фильтры для поиска продукта")
            ProductSearchDto filter
    ) {
        return ResponseEntity.ok(service.search(filter));
    }

    @Operation(summary = "Удалить продукт (только ADMIN)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID продукта", example = "1")
            @PathVariable Long id) {

        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Обновить продукт (только ADMIN)")
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDto> update(
            @Parameter(description = "ID продукта", example = "1")
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestBody @Valid ProductUpdateDto updateDto
    ) {
        return ResponseEntity.ok(service.update(id, updateDto));
    }
}
