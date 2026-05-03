package ru.alenavir.mini_ecommerce.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alenavir.mini_ecommerce.dto.order.OrderCreateDto;
import ru.alenavir.mini_ecommerce.dto.order.OrderItemCreateDto;
import ru.alenavir.mini_ecommerce.dto.order.OrderResponseDto;
import ru.alenavir.mini_ecommerce.dto.order.OrderUpdateDto;
import ru.alenavir.mini_ecommerce.entity.Order;
import ru.alenavir.mini_ecommerce.entity.OrderItem;
import ru.alenavir.mini_ecommerce.entity.OutboxEvent;
import ru.alenavir.mini_ecommerce.entity.Product;
import ru.alenavir.mini_ecommerce.entity.enums.OrderStatus;
import ru.alenavir.mini_ecommerce.exceptions.NotFoundException;
import ru.alenavir.mini_ecommerce.kafka.OrderCreatedEvent;
import ru.alenavir.mini_ecommerce.mapper.OrderMapper;
import ru.alenavir.mini_ecommerce.repo.OrderRepo;
import ru.alenavir.mini_ecommerce.repo.OutboxEventRepo;
import ru.alenavir.mini_ecommerce.repo.ProductRepo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepo repo;
    private final ProductRepo productRepo;
    private final OrderMapper mapper;
    private final OutboxEventRepo outboxEventRepo;
    private final MeterRegistry meterRegistry;
    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    private static final int LIMIT_ORDERS = 10;

    @Transactional  // одна транзакция — заказ + outbox событие
    public OrderResponseDto create(OrderCreateDto createDto) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            log.info("Создание заказа для userId={}", createDto.getUserId());

            Order order = mapper.toEntity(createDto);

            List<Long> productIds = createDto.getItems().stream()
                    .map(OrderItemCreateDto::getProductId)
                    .toList();

            List<Product> products = productRepo.findAllById(productIds);
            Map<Long, Product> productMap = products.stream()
                    .collect(Collectors.toMap(Product::getId, Function.identity()));

            List<OrderItem> items = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (OrderItemCreateDto itemDto : createDto.getItems()) {
                Product product = productMap.get(itemDto.getProductId());
                if (product == null) {
                    throw new NotFoundException("Product with id " + itemDto.getProductId() + " not found");
                }

                OrderItem item = new OrderItem();
                item.setOrder(order);
                item.setProduct(product);
                item.setQuantity(itemDto.getQuantity());
                item.setPrice(product.getPrice());
                totalAmount = totalAmount.add(
                        product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()))
                );
                items.add(item);
            }

            order.setItems(items);
            order.setTotalAmount(totalAmount);
            order.setStatus(OrderStatus.PROCESSING);
            LocalDateTime now = LocalDateTime.now();
            order.setCreatedAt(now);
            order.setUpdatedAt(now);

            Order saved = repo.save(order);

            // --- Сохраняем событие в Outbox (в той же транзакции!) ---
            OrderCreatedEvent event = new OrderCreatedEvent(
                    UUID.randomUUID().toString(),
                    saved.getId(),
                    saved.getUserId(),
                    productIds
            );

            OutboxEvent outbox = new OutboxEvent();
            outbox.setId(UUID.randomUUID());
            outbox.setEventType("OrderCreatedEvent");
            outbox.setPayload(objectMapper.writeValueAsString(event));
            outbox.setStatus(OutboxEvent.OutboxStatus.PENDING);
            outbox.setCreatedAt(now);
            outbox.setAttempts(0);
            outboxEventRepo.save(outbox);

            log.info("Заказ создан orderId={}, outbox событие сохранено", saved.getId());
            meterRegistry.counter("orders.created").increment();

            Cache cache = cacheManager.getCache("lastOrders");
            if (cache != null) {
                cache.evict(saved.getUserId());
            }

            return mapper.toDto(saved);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка сериализации события", e);
        } finally {
            timer.stop(meterRegistry.timer("orders.create.time"));
        }
    }

    @Cacheable(value = "lastOrders", key = "#userId")
    public List<OrderResponseDto> findLastOrders(Long userId) {
        List<Order> orders = repo.findLastOrdersByUserId(userId, PageRequest.of(0, LIMIT_ORDERS));
        log.info("Получение последних {} заказов для userId={}", LIMIT_ORDERS, userId);
        return mapper.toDtoList(orders);
    }

    @Cacheable(value = "orders", key = "#id")
    public OrderResponseDto findById(Long id) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            Order order = repo.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Заказ с id={} не найден", id);
                        return new NotFoundException("Order with id " + id + " not found");
                    });

            log.info("Заказ найден: orderId={}", id);
            return mapper.toDto(order);
        } finally {
            timer.stop(meterRegistry.timer("orders.findById.time"));
        }
    }

    public List<OrderResponseDto> findAll() {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            List<Order> orders = repo.findAllWithItemsAndProducts();
            log.info("Получение всех заказов, count={}", orders.size());
            return mapper.toDtoList(orders);
        } finally {
            timer.stop(meterRegistry.timer("orders.findAll.time"));
        }
    }

    @CacheEvict(value = "orders", key = "#id")
    public void delete(Long id) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            Order order = repo.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Попытка удалить несуществующий заказ: orderId={}", id);
                        return new NotFoundException("Order with id " + id + " not found");
                    });

            repo.delete(order);
            log.info("Заказ удален: orderId={}", id);
            meterRegistry.counter("orders.deleted").increment();
        } finally {
            timer.stop(meterRegistry.timer("orders.delete.time"));
        }
    }

    @CachePut(value = "orders", key = "#id")
    public OrderResponseDto update(Long id, OrderUpdateDto updateDto) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            Order order = repo.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Попытка обновить несуществующий заказ: orderId={}", id);
                        return new NotFoundException("Order with id " + id + " not found");
                    });

            mapper.updateOrderFromDto(updateDto, order);
            order.setUpdatedAt(LocalDateTime.now());

            Order saved = repo.save(order);
            log.info("Заказ обновлен: orderId={}", id);
            meterRegistry.counter("orders.updated").increment();

            return mapper.toDto(saved);
        } finally {
            timer.stop(meterRegistry.timer("orders.update.time"));
        }
    }

    public OrderResponseDto changeStatus(Long orderId, OrderStatus newStatus) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            Order order = repo.findById(orderId)
                    .orElseThrow(() -> {
                        log.warn("Попытка изменить статус несуществующего заказа: orderId={}", orderId);
                        return new NotFoundException("Order with id " + orderId + " not found");
                    });

            OrderStatus current = order.getStatus();
            if (!current.canChangeTo(newStatus)) {
                log.warn("Невозможно изменить статус заказа orderId={} с {} на {}", orderId, current, newStatus);
                throw new IllegalStateException("Cannot change status from " + current + " to " + newStatus);
            }

            order.setStatus(newStatus);
            order.setUpdatedAt(LocalDateTime.now());
            Order saved = repo.save(order);

            log.info("Статус заказа изменен: orderId={}, newStatus={}", orderId, newStatus);
            meterRegistry.counter("orders.status.changed").increment();

            return mapper.toDto(saved);
        } finally {
            timer.stop(meterRegistry.timer("orders.changeStatus.time"));
        }
    }
}
