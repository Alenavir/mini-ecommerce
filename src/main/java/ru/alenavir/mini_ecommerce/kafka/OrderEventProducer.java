package ru.alenavir.mini_ecommerce.kafka;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final MeterRegistry meterRegistry;

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        try {
            kafkaTemplate.send("order-events", event);

            log.info("OrderCreatedEvent отправлено, orderId={}", event.getOrderId());

            meterRegistry.counter("kafka.order.events.sent").increment();
        } catch (Exception ex) {
            log.error("Ошибка при отправке OrderCreatedEvent, orderId={}", event.getOrderId(), ex);

            meterRegistry.counter("kafka.order.events.failed").increment();
        }
    }
}