package ru.alenavir.mini_ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.alenavir.mini_ecommerce.dto.product.ProductCreateDto;
import ru.alenavir.mini_ecommerce.dto.product.ProductResponseDto;
import ru.alenavir.mini_ecommerce.dto.product.ProductSearchDto;
import ru.alenavir.mini_ecommerce.dto.product.ProductUpdateDto;
import ru.alenavir.mini_ecommerce.entity.Product;
import ru.alenavir.mini_ecommerce.entity.enums.Category;
import ru.alenavir.mini_ecommerce.exceptions.NotFoundException;
import ru.alenavir.mini_ecommerce.mapper.ProductMapper;
import ru.alenavir.mini_ecommerce.repo.ProductRepo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepo repo;
    private final ProductMapper mapper;

    public ProductResponseDto create(ProductCreateDto dto) {

        Product product = mapper.toEntity(dto);

        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        return mapper.toDto(repo.save(product));
    }

    public List<ProductResponseDto> search(ProductSearchDto filter) {

        List<Product> products = repo.search(
                filter.getName(),
                filter.getSku(),
                filter.getCategory(),
                filter.getMinPrice(),
                filter.getMaxPrice()
        );

        return mapper.toList(products);
    }

    @Cacheable(value = "products", key = "#id")
    public ProductResponseDto findById(Long id) {

        Product product = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Product with id " + id + " not found"));

        return mapper.toDto(product);
    }

    @CachePut(value = "products", key = "#id")
    public ProductResponseDto update(Long id, ProductUpdateDto dto) {

        Product exist = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Product with id " + id + " not found"));

        mapper.updateProductFromDto(dto, exist);

        exist.setUpdatedAt(LocalDateTime.now());

        return mapper.toDto(repo.save(exist));
    }

    @CacheEvict(value = "products", key = "#id")
    public void delete(Long id) {

        Product product = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Product with id " + id + " not found"));

        repo.delete(product);
    }
}