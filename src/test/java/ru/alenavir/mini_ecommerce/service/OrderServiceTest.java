package ru.alenavir.mini_ecommerce.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import ru.alenavir.mini_ecommerce.dto.order.OrderCreateDto;
import ru.alenavir.mini_ecommerce.dto.order.OrderItemCreateDto;
import ru.alenavir.mini_ecommerce.dto.order.OrderResponseDto;
import ru.alenavir.mini_ecommerce.entity.Order;
import ru.alenavir.mini_ecommerce.entity.Product;
import ru.alenavir.mini_ecommerce.entity.enums.OrderStatus;
import ru.alenavir.mini_ecommerce.kafka.OrderEventProducer;
import ru.alenavir.mini_ecommerce.mapper.OrderMapper;
import ru.alenavir.mini_ecommerce.repo.OrderRepo;
import ru.alenavir.mini_ecommerce.repo.ProductRepo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepo orderRepo;

    @Mock
    private ProductRepo productRepo;

    @Mock
    private OrderMapper mapper;

    @Mock
    private OrderEventProducer orderEventProducer;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    private MeterRegistry meterRegistry;
    private OrderService orderService;

    private OrderCreateDto createDto;
    private Product product;
    private Order order;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();

        orderService = new OrderService(
                orderRepo,
                productRepo,
                mapper,
                orderEventProducer,
                meterRegistry,
                cacheManager
        );

        product = new Product();
        product.setId(1L);
        product.setPrice(BigDecimal.valueOf(100));

        OrderItemCreateDto itemDto = new OrderItemCreateDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);

        createDto = new OrderCreateDto();
        createDto.setUserId(10L);
        createDto.setItems(List.of(itemDto));

        order = new Order();
        order.setId(1L);
        order.setUserId(10L);
        order.setTotalAmount(BigDecimal.valueOf(200));
        order.setStatus(OrderStatus.PROCESSING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void create_shouldCreateOrderSuccessfully() {
        when(productRepo.findAllById(anyList())).thenReturn(List.of(product));
        when(orderRepo.save(any(Order.class))).thenReturn(order);
        when(mapper.toEntity(createDto)).thenReturn(new Order());
        when(mapper.toDto(order)).thenReturn(new OrderResponseDto());

        when(cacheManager.getCache("lastOrders")).thenReturn(cache);

        OrderResponseDto result = orderService.create(createDto);

        assertNotNull(result);

        verify(orderRepo).save(any(Order.class));
        verify(orderEventProducer).sendOrderCreatedEvent(any());

        verify(cache).evict(order.getUserId());
    }

    @Test
    void create_shouldThrowException_whenProductNotFound() {
        when(productRepo.findAllById(anyList())).thenReturn(Collections.emptyList());
        when(mapper.toEntity(createDto)).thenReturn(new Order());

        assertThrows(RuntimeException.class,
                () -> orderService.create(createDto));
    }

    @Test
    void findById_shouldReturnOrder() {
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(mapper.toDto(order)).thenReturn(new OrderResponseDto());

        OrderResponseDto result = orderService.findById(1L);

        assertNotNull(result);
        verify(orderRepo).findById(1L);
    }

    @Test
    void findById_shouldThrowException_whenNotFound() {
        when(orderRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> orderService.findById(1L));
    }

    @Test
    void delete_shouldDeleteOrder() {
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        orderService.delete(1L);

        verify(orderRepo).delete(order);
    }

    @Test
    void changeStatus_shouldChangeStatusSuccessfully() {
        order.setStatus(OrderStatus.PROCESSING);

        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepo.save(any(Order.class))).thenReturn(order);
        when(mapper.toDto(order)).thenReturn(new OrderResponseDto());

        OrderResponseDto result =
                orderService.changeStatus(1L, OrderStatus.PAID);

        assertNotNull(result);
        assertEquals(OrderStatus.PAID, order.getStatus());
    }
}