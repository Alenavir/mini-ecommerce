package ru.alenavir.mini_ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alenavir.mini_ecommerce.dto.order.OrderCreateDto;
import ru.alenavir.mini_ecommerce.dto.order.OrderItemCreateDto;
import ru.alenavir.mini_ecommerce.dto.order.OrderResponseDto;
import ru.alenavir.mini_ecommerce.dto.order.OrderUpdateDto;
import ru.alenavir.mini_ecommerce.entity.Order;
import ru.alenavir.mini_ecommerce.entity.OrderItem;
import ru.alenavir.mini_ecommerce.entity.Product;
import ru.alenavir.mini_ecommerce.entity.enums.OrderStatus;
import ru.alenavir.mini_ecommerce.exceptions.NotFoundException;
import ru.alenavir.mini_ecommerce.kafka.OrderCreatedEvent;
import ru.alenavir.mini_ecommerce.kafka.OrderEventProducer;
import ru.alenavir.mini_ecommerce.mapper.OrderMapper;
import ru.alenavir.mini_ecommerce.repo.OrderRepo;
import ru.alenavir.mini_ecommerce.repo.ProductRepo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepo repo;
    private final ProductRepo productRepo;
    private final OrderMapper mapper;
    private final OrderEventProducer orderEventProducer;

    @Transactional
    public OrderResponseDto create(OrderCreateDto createDto) {
        Order order = mapper.toEntity(createDto);

        List<Long> productIds = createDto.getItems()
                .stream()
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

            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
            items.add(item);
        }

        order.setItems(items);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PROCESSING);
        LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        Order saved = repo.save(order);

        // --- Kafka ---
        OrderCreatedEvent event = new OrderCreatedEvent(
                saved.getId(),
                saved.getUserId(),
                productIds
        );
        orderEventProducer.sendOrderCreatedEvent(event);

        return mapper.toDto(saved);
    }

    public OrderResponseDto findById(Long id) {
        Order order = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Order with id " + id + " not found"));

        return mapper.toDto(order);
    }

    public List<OrderResponseDto> findAll() {
        return mapper.toDtoList(repo.findAllWithItemsAndProducts());
    }

    public void delete(Long id) {
        Order order = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Order with id " + id + " not found"));

        repo.delete(order);
    }

    public OrderResponseDto update(Long id, OrderUpdateDto updateDto) {
        Order order = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Order with id " + id + " not found"));

        mapper.updateOrderFromDto(updateDto, order);

        order.setUpdatedAt(LocalDateTime.now());

        return mapper.toDto(repo.save(order));
    }

    public OrderResponseDto changeStatus(Long orderId, OrderStatus newStatus) {

        Order order = repo.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order with id " + orderId + " not found"));

        OrderStatus current = order.getStatus();

        if (!current.canChangeTo(newStatus)) {
            throw new IllegalStateException(
                    "Cannot change status from " + current + " to " + newStatus
            );
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        return mapper.toDto(repo.save(order));
    }
}
