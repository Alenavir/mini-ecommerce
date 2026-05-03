package ru.alenavir.mini_ecommerce.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${kafka.topics.order-events}")
    private String orderEventsTopic;

    @Value("${kafka.topics.order-events-dlt}")
    private String orderEventsDlt;

    @Value("${kafka.topics.partitions}")
    private int partitions;

    @Value("${kafka.topics.replicas}")
    private int replicas;

    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder.name(orderEventsTopic)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    @Bean
    public NewTopic orderEventsDLT() {
        return TopicBuilder.name(orderEventsDlt)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }
}
