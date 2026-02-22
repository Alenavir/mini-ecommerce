package ru.alenavir.mini_ecommerce.kafka;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class OrderCreatedEvent {
    private Long orderId;
    private Long userId;
    private List<Long> productIds;

    public OrderCreatedEvent() {}

    public OrderCreatedEvent(Long orderId, Long userId, List<Long> productIds) {
        this.orderId = orderId;
        this.userId = userId;
        this.productIds = productIds;
    }
}