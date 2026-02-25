package ru.alenavir.mini_ecommerce.kafka;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alenavir.mini_ecommerce.entity.Order;
import ru.alenavir.mini_ecommerce.entity.enums.OrderStatus;
import ru.alenavir.mini_ecommerce.exceptions.NotFoundException;
import ru.alenavir.mini_ecommerce.repo.OrderRepo;
import ru.alenavir.mini_ecommerce.repo.ProductRepo;
import ru.alenavir.mini_ecommerce.repo.projection.ProductStockProjection;
import ru.alenavir.mini_ecommerce.service.ProductBatchUpdateService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final OrderRepo orderRepository;
    private final ProductRepo productRepository;
    private final ProductBatchUpdateService batchUpdateService;

    @KafkaListener(topics = "order-events", groupId = "order-group")
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        // Идемпотентность
        if (order.getStatus() != OrderStatus.PROCESSING) {
            log.info("Order {} already processed", order.getId());
            return;
        }

        // Загрузка продукты через проекцию
        List<ProductStockProjection> products = productRepository.findByIdIn(event.getProductIds());
        Map<Long, Integer> productStockMap = new HashMap<>();
        for (ProductStockProjection p : products) {
            productStockMap.put(p.getId(), p.getQuantityInStock());
        }

        // Проверка наличие товара
        boolean allAvailable = order.getItems().stream()
                .allMatch(item -> productStockMap.getOrDefault(item.getProduct().getId(), 0) >= item.getQuantity());

        if (allAvailable) {
            // Формирование Map для batch update
            Map<Long, Integer> updatedQuantities = order.getItems().stream()
                    .collect(Collectors.toMap(
                            item -> item.getProduct().getId(),
                            item -> productStockMap.get(item.getProduct().getId()) - item.getQuantity(),
                            (oldVal, newVal) -> newVal // исключение падения при дублирующихся productId
                    ));

            batchUpdateService.batchUpdateQuantities(updatedQuantities);

            order.setStatus(OrderStatus.PAID);
            log.info("Order {} status updated to PAID", order.getId());
        } else {
            order.setStatus(OrderStatus.CANCELLED);
            log.info("Order {} status updated to CANCELLED due to insufficient stock", order.getId());
        }

        orderRepository.save(order);
    }
}