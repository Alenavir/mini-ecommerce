package ru.alenavir.mini_ecommerce.kafka;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alenavir.mini_ecommerce.entity.Order;
import ru.alenavir.mini_ecommerce.entity.ProcessedEvent;
import ru.alenavir.mini_ecommerce.entity.Product;
import ru.alenavir.mini_ecommerce.entity.enums.OrderStatus;
import ru.alenavir.mini_ecommerce.exceptions.NotFoundException;
import ru.alenavir.mini_ecommerce.repo.OrderRepo;
import ru.alenavir.mini_ecommerce.repo.ProcessedEventRepo;
import ru.alenavir.mini_ecommerce.repo.ProductRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final OrderRepo orderRepository;
    private final ProductRepo productRepository;
    private final ProcessedEventRepo processedEventRepo;

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
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Получено событие orderId={}, eventId={}", event.getOrderId(), event.getEventId());

        orderProcessingTimer.record(() -> {
            if (isAlreadyProcessed(event.getEventId())) return;

            Order order = getOrderInProcessing(event.getOrderId());
            if (order == null) return;

            processOrder(order);
            markAsProcessed(event.getEventId());
        });
    }

    private boolean isAlreadyProcessed(String eventId) {
        if (processedEventRepo.existsById(eventId)) {
            log.warn("Событие {} уже обработано, пропускаем", eventId);
            return true;
        }
        return false;
    }

    // Возвращает null если заказ уже не в PROCESSING — это валидный skip, не ошибка
    private Order getOrderInProcessing(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ не найден: " + orderId));

        if (order.getStatus() != OrderStatus.PROCESSING) {
            log.info("Заказ {} уже в статусе {}, пропускаем", order.getId(), order.getStatus());
            return null;
        }
        return order;
    }

    private void processOrder(Order order) {
        Map<Long, Product> productMap = loadProducts(order);

        if (hasStockShortage(order, productMap)) {
            cancelOrder(order);
        } else {
            fulfillOrder(order, productMap);
        }
    }

    private Map<Long, Product> loadProducts(Order order) {
        List<Long> productIds = order.getItems().stream()
                .map(i -> i.getProduct().getId())
                .toList();

        return productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
    }

    private boolean hasStockShortage(Order order, Map<Long, Product> productMap) {
        return order.getItems().stream()
                .anyMatch(item ->
                        productMap.get(item.getProduct().getId()).getQuantityInStock()
                                < item.getQuantity()
                );
    }

    private void cancelOrder(Order order) {
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        ordersCancelled.increment();
        log.warn("Заказ {} отменён — нехватка товара", order.getId());
    }

    private void fulfillOrder(Order order, Map<Long, Product> productMap) {
        order.getItems().forEach(item -> {
            Product product = productMap.get(item.getProduct().getId());
            product.setQuantityInStock(product.getQuantityInStock() - item.getQuantity());
        });
        productRepository.saveAll(productMap.values());

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
        ordersProcessed.increment();
        log.info("Заказ {} → PAID", order.getId());
    }

    private void markAsProcessed(String eventId) {
        ProcessedEvent processed = new ProcessedEvent();
        processed.setEventId(eventId);
        processed.setProcessedAt(LocalDateTime.now());
        processedEventRepo.save(processed);
    }
}