package ru.alenavir.mini_ecommerce.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alenavir.mini_ecommerce.dto.product.ProductCreateDto;
import ru.alenavir.mini_ecommerce.dto.product.ProductResponseDto;
import ru.alenavir.mini_ecommerce.dto.product.ProductUpdateDto;
import ru.alenavir.mini_ecommerce.entity.Product;
import ru.alenavir.mini_ecommerce.exceptions.NotFoundException;
import ru.alenavir.mini_ecommerce.mapper.ProductMapper;
import ru.alenavir.mini_ecommerce.repo.ProductRepo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
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

    @InjectMocks
    private ProductService service;

    private final Product product = new Product() {{
        setId(1L);
        setName("Test Product");
        setDescription("Description");
        setPrice(BigDecimal.valueOf(100));
        setQuantityInStock(10);
        setCreatedAt(LocalDateTime.now());
        setUpdatedAt(LocalDateTime.now());
    }};

    private final ProductCreateDto createDto = new ProductCreateDto() {{
        setName("Test Product");
        setDescription("Description");
        setPrice(BigDecimal.valueOf(100));
        setQuantityInStock(10);
    }};

    private final ProductUpdateDto updateDto = new ProductUpdateDto() {{
        setName("Updated Name");
    }};

    private final ProductResponseDto responseDto = new ProductResponseDto() {{
        setId(1L);
        setName("Test Product");
    }};

    @Test
    void create_ShouldReturnResponseDto() {
        when(mapper.toEntity(createDto)).thenReturn(product);
        when(repo.save(product)).thenReturn(product);
        when(mapper.toResponse(product)).thenReturn(responseDto);

        ProductResponseDto result = service.create(createDto);

        assertNotNull(result);
        assertEquals(responseDto.getId(), result.getId());
        verify(repo).save(product);
    }

    @Test
    void findAll_ShouldReturnList() {
        List<Product> products = Collections.singletonList(product);
        List<ProductResponseDto> responses = Collections.singletonList(responseDto);

        when(repo.findAll()).thenReturn(products);
        when(mapper.toResponseList(products)).thenReturn(responses);

        List<ProductResponseDto> result = service.findAll();

        assertEquals(1, result.size());
        assertEquals(responseDto.getId(), result.get(0).getId());
    }

    @Test
    void findById_WhenFound_ShouldReturnResponse() {
        when(repo.findById(1L)).thenReturn(Optional.of(product));
        when(mapper.toResponse(product)).thenReturn(responseDto);

        ProductResponseDto result = service.findById(1L);

        assertEquals(responseDto.getId(), result.getId());
    }

    @Test
    void findById_WhenNotFound_ShouldThrow() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void update_WhenFound_ShouldReturnUpdatedResponse() {
        when(repo.findById(1L)).thenReturn(Optional.of(product));
        // mapper обновляет поля в существующем объекте
        doNothing().when(mapper).updateProductFromDto(updateDto, product);
        when(repo.save(product)).thenReturn(product);
        when(mapper.toResponse(product)).thenReturn(responseDto);

        ProductResponseDto result = service.update(1L, updateDto);

        assertEquals(responseDto.getId(), result.getId());
        verify(mapper).updateProductFromDto(updateDto, product);
        verify(repo).save(product);
    }

    @Test
    void update_WhenNotFound_ShouldThrow() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.update(1L, updateDto));
    }

    @Test
    void delete_WhenFound_ShouldCallRepoDelete() {
        when(repo.findById(1L)).thenReturn(Optional.of(product));

        service.delete(1L);

        verify(repo).delete(product);
    }

    @Test
    void delete_WhenNotFound_ShouldThrow() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.delete(1L));
    }
}