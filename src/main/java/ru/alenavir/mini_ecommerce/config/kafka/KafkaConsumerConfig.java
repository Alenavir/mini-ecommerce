package ru.alenavir.mini_ecommerce.config.kafka;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import ru.alenavir.mini_ecommerce.kafka.OrderCreatedEvent;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent>
    kafkaListenerContainerFactory(
            ConsumerFactory<String, OrderCreatedEvent> consumerFactory,
            DefaultErrorHandler errorHandler
    ) {

        ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties()
                .setAckMode(ContainerProperties.AckMode.RECORD);

        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

    @Bean
    public DefaultErrorHandler errorHandler(
            KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {

        FixedBackOff backOff = new FixedBackOff(2000L, 3);

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, ex) ->
                                new TopicPartition(record.topic() + ".DLT",
                                        record.partition())
                );

        DefaultErrorHandler handler =
                new DefaultErrorHandler(recoverer, backOff);

        handler.addNotRetryableExceptions(IllegalArgumentException.class);

        handler.setRetryListeners((record, ex, attempt) ->
                System.out.println("Retry attempt: " + attempt));

        return handler;
    }
}
