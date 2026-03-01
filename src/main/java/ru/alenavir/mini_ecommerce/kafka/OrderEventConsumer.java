package ru.alenavir.mini_ecommerce.kafka;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class OrderEventConsumer {

    private final OrderRepo orderRepository;
    private final ProductRepo productRepository;

    // Метрики: количество обработанных и отменённых заказов, время обработки заказа
    private final Counter ordersProcessed = Counter.builder("orders_processed_total")
            .description("Количество успешно обработанных заказов")
            .register(Metrics.globalRegistry);

    private final Counter ordersCancelled = Counter.builder("orders_cancelled_total")
            .description("Количество заказов, отменённых из-за нехватки товара")
            .register(Metrics.globalRegistry);

    private final Timer orderProcessingTimer = Timer.builder("order_processing_duration_seconds")
            .description("Время обработки заказа")
            .register(Metrics.globalRegistry);

    @KafkaListener(topics = "order-events", groupId = "order-group")
    @Retryable(
            value = OptimisticLockException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000)
    )
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {

        log.info("Получено событие OrderCreatedEvent для заказа id={}", event.getOrderId());

        orderProcessingTimer.record(() -> {
            Order order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new NotFoundException("Заказ не найден"));

            if (order.getStatus() != OrderStatus.PROCESSING) {
                log.info("Заказ {} уже обработан, пропускаем", order.getId());
                return;
            }

            List<Product> products = productRepository.findAllById( order.getItems()
                    .stream().map(i -> i.getProduct().getId()).toList());
            Map<Long, Product> productMap = products.stream()
                    .collect(Collectors.toMap(Product::getId, p -> p));

            boolean allAvailable = order.getItems().stream()
                    .allMatch(item -> productMap.get(item.getProduct().getId()).getQuantityInStock() >= item.getQuantity());

            if (!allAvailable) {
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
                ordersCancelled.increment();
                log.warn("Заказ {} отменён из-за нехватки товара", order.getId());
                return;
            }

            for (OrderItem item : order.getItems()) {
                Product product = productMap.get(item.getProduct().getId());
                product.setQuantityInStock(product.getQuantityInStock() - item.getQuantity());
                productRepository.save(product);
            }

            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
            ordersProcessed.increment();
            log.info("Заказ {} успешно обработан: статус PAID", order.getId());
        });
    }
}