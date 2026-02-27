package ru.alenavir.mini_ecommerce.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.alenavir.mini_ecommerce.dto.product.ProductCreateDto;
import ru.alenavir.mini_ecommerce.dto.product.ProductResponseDto;
import ru.alenavir.mini_ecommerce.dto.product.ProductUpdateDto;
import ru.alenavir.mini_ecommerce.entity.enums.Category;
import ru.alenavir.mini_ecommerce.service.ProductService;

import java.math.BigDecimal;
import java.util.List;


@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDto> create(
            @Valid @RequestBody ProductCreateDto createDto
    ) {
        ProductResponseDto response = service.create(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getById(@PathVariable Long id) {
        ProductResponseDto response = service.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        return ResponseEntity.ok(
                service.search(name, sku, category, minPrice, maxPrice)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateDto updateDto
    ) {
        ProductResponseDto response = service.update(id, updateDto);
        return ResponseEntity.ok(response);
    }
}
