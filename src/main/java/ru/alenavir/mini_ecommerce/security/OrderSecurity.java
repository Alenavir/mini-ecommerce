package ru.alenavir.mini_ecommerce.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.alenavir.mini_ecommerce.repo.OrderRepo;

@Component("orderSecurity")
@RequiredArgsConstructor
public class OrderSecurity {

    private final OrderRepo orderRepository;

    public boolean isOwner(Long orderId, Long userId) {
        return orderRepository.existsByIdAndUserId(orderId, userId);
    }
}
