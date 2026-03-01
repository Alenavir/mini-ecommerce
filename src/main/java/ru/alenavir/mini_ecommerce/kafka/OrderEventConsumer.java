package ru.alenavir.mini_ecommerce.kafka;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alenavir.mini_ecommerce.entity.Order;
import ru.alenavir.mini_ecommerce.entity.OrderItem;
import ru.alenavir.mini_ecommerce.entity.Product;
import ru.alenavir.mini_ecommerce.entity.enums.OrderStatus;
import ru.alenavir.mini_ecommerce.exceptions.NotFoundException;
import ru.alenavir.mini_ecommerce.repo.OrderRepo;
import ru.alenavir.mini_ecommerce.repo.ProductRepo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final OrderRepo orderRepository;
    private final ProductRepo productRepository;

    @KafkaListener(topics = "order-events", groupId = "order-group")
    @Retryable(
            value = OptimisticLockException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000)
    )
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {

        log.info("Received OrderCreatedEvent for orderId={}", event.getOrderId());

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.PROCESSING) {
            log.info("Order {} already processed, skipping", order.getId());
            return;
        }

        List<Product> products = productRepository.findAllById(
                order.getItems().stream().map(i -> i.getProduct().getId()).toList()
        );
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        boolean allAvailable = order.getItems().stream()
                .allMatch(item -> productMap.get(item.getProduct().getId()).getQuantityInStock() >= item.getQuantity());

        if (!allAvailable) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            log.info("Order {} cancelled due to insufficient stock", order.getId());
            return;
        }

        for (OrderItem item : order.getItems()) {
            Product product = productMap.get(item.getProduct().getId());
            product.setQuantityInStock(product.getQuantityInStock() - item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
        log.info("Order {} processed successfully: PAID", order.getId());
    }
}