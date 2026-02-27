package ru.alenavir.mini_ecommerce.config.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.alenavir.mini_ecommerce.kafka.OrderCreatedEvent;

import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, OrderCreatedEvent> producerFactory(
            KafkaProperties properties
    ) {
        Map<String, Object> config = properties.buildProducerProperties();

        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 10);
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate(
            ProducerFactory<String, OrderCreatedEvent> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }
}
