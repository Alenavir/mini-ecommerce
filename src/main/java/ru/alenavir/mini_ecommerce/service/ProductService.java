package ru.alenavir.mini_ecommerce.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.alenavir.mini_ecommerce.dto.product.ProductCreateDto;
import ru.alenavir.mini_ecommerce.dto.product.ProductResponseDto;
import ru.alenavir.mini_ecommerce.dto.product.ProductSearchDto;
import ru.alenavir.mini_ecommerce.dto.product.ProductUpdateDto;
import ru.alenavir.mini_ecommerce.entity.Product;
import ru.alenavir.mini_ecommerce.exceptions.NotFoundException;
import ru.alenavir.mini_ecommerce.mapper.ProductMapper;
import ru.alenavir.mini_ecommerce.repo.ProductRepo;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepo repo;
    private final ProductMapper mapper;
    private final MeterRegistry meterRegistry;

    public ProductResponseDto create(ProductCreateDto dto) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            log.info("Создание продукта: name={}, sku={}", dto.getName(), dto.getSku());

            Product product = mapper.toEntity(dto);
            LocalDateTime now = LocalDateTime.now();
            product.setCreatedAt(now);
            product.setUpdatedAt(now);

            Product saved = repo.save(product);

            log.info("Продукт создан: productId={}, name={}", saved.getId(), saved.getName());
            meterRegistry.counter("products.created").increment();

            return mapper.toDto(saved);
        } finally {
            timer.stop(meterRegistry.timer("products.create.time"));
        }
    }

    public List<ProductResponseDto> search(ProductSearchDto filter) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            List<Product> products = repo.search(
                    filter.getName(),
                    filter.getSku(),
                    filter.getCategory(),
                    filter.getMinPrice(),
                    filter.getMaxPrice()
            );

            log.info("Поиск продуктов выполнен: count={}", products.size());
            return mapper.toList(products);
        } finally {
            timer.stop(meterRegistry.timer("products.search.time"));
        }
    }

    @Cacheable(value = "products", key = "#id")
    public ProductResponseDto findById(Long id) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            Product product = repo.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Продукт с id={} не найден", id);
                        return new NotFoundException("Product with id " + id + " not found");
                    });

            log.info("Продукт найден: productId={}", id);
            return mapper.toDto(product);
        } finally {
            timer.stop(meterRegistry.timer("products.findById.time"));
        }
    }

    @CachePut(value = "products", key = "#id")
    public ProductResponseDto update(Long id, ProductUpdateDto dto) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            Product exist = repo.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Попытка обновить несуществующий продукт: productId={}", id);
                        return new NotFoundException("Product with id " + id + " not found");
                    });

            mapper.updateProductFromDto(dto, exist);
            exist.setUpdatedAt(LocalDateTime.now());

            Product saved = repo.save(exist);

            log.info("Продукт обновлен: productId={}, name={}", saved.getId(), saved.getName());
            meterRegistry.counter("products.updated").increment();

            return mapper.toDto(saved);
        } finally {
            timer.stop(meterRegistry.timer("products.update.time"));
        }
    }

    @CacheEvict(value = "products", key = "#id")
    public void delete(Long id) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            Product product = repo.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Попытка удалить несуществующий продукт: productId={}", id);
                        return new NotFoundException("Product with id " + id + " not found");
                    });

            repo.delete(product);
            log.info("Продукт удален: productId={}, name={}", product.getId(), product.getName());
            meterRegistry.counter("products.deleted").increment();
        } finally {
            timer.stop(meterRegistry.timer("products.delete.time"));
        }
    }
}