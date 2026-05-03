package ru.alenavir.mini_ecommerce.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alenavir.mini_ecommerce.entity.OutboxEvent;
import ru.alenavir.mini_ecommerce.repo.OutboxEventRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final OutboxEventRepo outboxEventRepo;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    private static final int MAX_ATTEMPTS = 5;

    @Scheduled(fixedDelay = 5000)  // каждые 5 секунд
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pending = outboxEventRepo
                .findByStatusOrderByCreatedAtAsc(OutboxEvent.OutboxStatus.PENDING);

        if (pending.isEmpty()) return;

        log.info("Outbox: найдено {} событий для отправки", pending.size());

        for (OutboxEvent outbox : pending) {
            try {
                OrderCreatedEvent event = objectMapper.readValue(
                        outbox.getPayload(), OrderCreatedEvent.class
                );

                // Синхронная отправка — ожидание подтверждения от Kafka
                kafkaTemplate.send("order-events", event.getOrderId().toString(), event)
                        .get(10, TimeUnit.SECONDS);

                outbox.setStatus(OutboxEvent.OutboxStatus.SENT);
                outboxEventRepo.save(outbox);

                meterRegistry.counter("outbox.events.sent").increment();
                log.info("Outbox: событие {} отправлено в Kafka", outbox.getId());

            } catch (Exception ex) {
                handleFailedAttempt(outbox, ex);
            }
        }
    }

    private void handleFailedAttempt(OutboxEvent outbox, Exception ex) {
        int attempts = outbox.getAttempts() + 1;
        outbox.setAttempts(attempts);
        outbox.setLastAttemptAt(LocalDateTime.now());

        if (attempts >= MAX_ATTEMPTS) {
            outbox.setStatus(OutboxEvent.OutboxStatus.FAILED);
            outboxEventRepo.save(outbox);

            // Алерт — в продакшене здесь был бы PagerDuty/Slack
            meterRegistry.counter("outbox.events.failed").increment();
            log.error("ALERT: Outbox событие {} не удалось отправить после {} попыток. " +
                    "Требуется ручное вмешательство!", outbox.getId(), MAX_ATTEMPTS, ex);
        } else {
            outboxEventRepo.save(outbox);
            log.warn("Outbox: попытка {}/{} для события {} не удалась",
                    attempts, MAX_ATTEMPTS, outbox.getId(), ex);
        }
    }
}