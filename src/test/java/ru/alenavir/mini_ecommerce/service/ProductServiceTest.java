package ru.alenavir.mini_ecommerce.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alenavir.mini_ecommerce.dto.product.ProductCreateDto;
import ru.alenavir.mini_ecommerce.dto.product.ProductResponseDto;
import ru.alenavir.mini_ecommerce.dto.product.ProductSearchDto;
import ru.alenavir.mini_ecommerce.dto.product.ProductUpdateDto;
import ru.alenavir.mini_ecommerce.entity.Product;
import ru.alenavir.mini_ecommerce.exceptions.NotFoundException;
import ru.alenavir.mini_ecommerce.mapper.ProductMapper;
import ru.alenavir.mini_ecommerce.repo.ProductRepo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepo repo;

    @Mock
    private ProductMapper mapper;

    private MeterRegistry meterRegistry;
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();

        productService = new ProductService(
                repo,
                mapper,
                meterRegistry
        );

        product = new Product();
        product.setId(1L);
        product.setName("Test product");
        product.setSku("SKU-1");
        product.setPrice(BigDecimal.valueOf(100));
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void create_shouldCreateProductSuccessfully() {
        ProductCreateDto dto = new ProductCreateDto();
        dto.setName("Test product");
        dto.setSku("SKU-1");

        when(mapper.toEntity(dto)).thenReturn(new Product());
        when(repo.save(any(Product.class))).thenReturn(product);
        when(mapper.toDto(product)).thenReturn(new ProductResponseDto());

        ProductResponseDto result = productService.create(dto);

        assertNotNull(result);
        verify(repo).save(any(Product.class));
    }

    @Test
    void search_shouldReturnProducts() {
        ProductSearchDto filter = new ProductSearchDto();

        when(repo.search(any(), any(), any(), any(), any()))
                .thenReturn(List.of(product));
        when(mapper.toList(anyList()))
                .thenReturn(List.of(new ProductResponseDto()));

        List<ProductResponseDto> result = productService.search(filter);

        assertEquals(1, result.size());
        verify(repo).search(any(), any(), any(), any(), any());
    }

    @Test
    void findById_shouldReturnProduct() {
        when(repo.findById(1L)).thenReturn(Optional.of(product));
        when(mapper.toDto(product)).thenReturn(new ProductResponseDto());

        ProductResponseDto result = productService.findById(1L);

        assertNotNull(result);
        verify(repo).findById(1L);
    }

    @Test
    void findById_shouldThrowException_whenNotFound() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> productService.findById(1L));
    }

    @Test
    void update_shouldUpdateProductSuccessfully() {
        ProductUpdateDto dto = new ProductUpdateDto();

        when(repo.findById(1L)).thenReturn(Optional.of(product));
        when(repo.save(any(Product.class))).thenReturn(product);
        when(mapper.toDto(product)).thenReturn(new ProductResponseDto());

        ProductResponseDto result = productService.update(1L, dto);

        assertNotNull(result);
        verify(repo).save(product);
    }

    @Test
    void update_shouldThrowException_whenNotFound() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> productService.update(1L, new ProductUpdateDto()));
    }

    @Test
    void delete_shouldDeleteProduct() {
        when(repo.findById(1L)).thenReturn(Optional.of(product));

        productService.delete(1L);

        verify(repo).delete(product);
    }

    @Test
    void delete_shouldThrowException_whenNotFound() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> productService.delete(1L));
    }
}