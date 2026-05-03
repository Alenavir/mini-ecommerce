package ru.alenavir.mini_ecommerce.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import ru.alenavir.mini_ecommerce.entity.OutboxEvent;
import ru.alenavir.mini_ecommerce.entity.Product;
import ru.alenavir.mini_ecommerce.entity.enums.OrderStatus;
import ru.alenavir.mini_ecommerce.exceptions.NotFoundException;
import ru.alenavir.mini_ecommerce.mapper.OrderMapper;
import ru.alenavir.mini_ecommerce.repo.OrderRepo;
import ru.alenavir.mini_ecommerce.repo.OutboxEventRepo;
import ru.alenavir.mini_ecommerce.repo.ProductRepo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private OrderMapper orderMapper;

    @Mock
    private OutboxEventRepo outboxEventRepo;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @Mock
    private ObjectMapper objectMapper;

    private MeterRegistry meterRegistry;
    private OrderService orderService;

    private OrderCreateDto createDto;
    private Product product;
    private Order order;
    private OrderItemCreateDto itemDto;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();

        orderService = new OrderService(
                orderRepo,
                productRepo,
                orderMapper,
                outboxEventRepo,
                meterRegistry,
                cacheManager,
                objectMapper
        );

        product = new Product();
        product.setId(1L);
        product.setPrice(BigDecimal.valueOf(100));

        itemDto = new OrderItemCreateDto();
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
        order.setItems(new ArrayList<>());
    }

    @Test
    void create_shouldCreateOrderSuccessfully() throws JsonProcessingException {
        Order emptyOrder = new Order();
        emptyOrder.setItems(new ArrayList<>());

        when(orderMapper.toEntity(createDto)).thenReturn(emptyOrder);
        when(productRepo.findAllById(anyList())).thenReturn(List.of(product));
        when(orderRepo.save(any(Order.class))).thenReturn(order);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"eventId\":\"uuid\"}");
        when(cacheManager.getCache("lastOrders")).thenReturn(cache);
        when(orderMapper.toDto(order)).thenReturn(new OrderResponseDto());

        OrderResponseDto result = orderService.create(createDto);

        assertNotNull(result);
        verify(orderRepo).save(any(Order.class));
        verify(outboxEventRepo).save(any(OutboxEvent.class));
        verify(cache).evict(10L);
    }

    @Test
    void create_shouldThrowException_whenProductNotFound() {
        when(orderMapper.toEntity(createDto)).thenReturn(new Order() {{ setItems(new ArrayList<>()); }});
        when(productRepo.findAllById(anyList())).thenReturn(Collections.emptyList());

        assertThrows(NotFoundException.class,
                () -> orderService.create(createDto));

        verify(orderRepo, never()).save(any());
        verify(outboxEventRepo, never()).save(any());
    }

    @Test
    void create_shouldNotEvictCache_whenCacheIsNull() throws JsonProcessingException {
        Order emptyOrder = new Order();
        emptyOrder.setItems(new ArrayList<>());

        when(orderMapper.toEntity(createDto)).thenReturn(emptyOrder);
        when(productRepo.findAllById(anyList())).thenReturn(List.of(product));
        when(orderRepo.save(any(Order.class))).thenReturn(order);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(cacheManager.getCache("lastOrders")).thenReturn(null);  // кеш недоступен
        when(orderMapper.toDto(order)).thenReturn(new OrderResponseDto());

        assertDoesNotThrow(() -> orderService.create(createDto));
        verify(cache, never()).evict(any());
    }

    @Test
    void findById_shouldReturnOrder() {
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(new OrderResponseDto());

        OrderResponseDto result = orderService.findById(1L);

        assertNotNull(result);
        verify(orderRepo).findById(1L);
    }

    @Test
    void findById_shouldThrowNotFoundException_whenOrderNotFound() {
        when(orderRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> orderService.findById(99L));
    }

    @Test
    void delete_shouldDeleteOrder() {
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        orderService.delete(1L);

        verify(orderRepo).delete(order);
    }

    @Test
    void delete_shouldThrowNotFoundException_whenOrderNotFound() {
        when(orderRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> orderService.delete(99L));

        verify(orderRepo, never()).delete(any());
    }

    @Test
    void changeStatus_shouldChangeStatusSuccessfully() {
        order.setStatus(OrderStatus.PROCESSING);

        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepo.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(new OrderResponseDto());

        OrderResponseDto result = orderService.changeStatus(1L, OrderStatus.PAID);

        assertNotNull(result);
        assertEquals(OrderStatus.PAID, order.getStatus());
        verify(orderRepo).save(order);
    }

    @Test
    void changeStatus_shouldThrowException_whenTransitionInvalid() {
        order.setStatus(OrderStatus.CANCELLED);  // из CANCELLED нельзя никуда

        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class,
                () -> orderService.changeStatus(1L, OrderStatus.PAID));

        verify(orderRepo, never()).save(any());
    }
}