package ru.alenavir.mini_ecommerce.dto.order;

import lombok.Getter;
import lombok.Setter;
import ru.alenavir.mini_ecommerce.entity.enums.OrderStatus;
import ru.alenavir.mini_ecommerce.entity.enums.PaymentMethod;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderResponseDto implements Serializable {

    private Long id;
    private Long userId;

    private List<OrderItemResponseDto> items;

    private BigDecimal totalAmount;

    private OrderStatus status;

    private String shippingAddress;

    private PaymentMethod paymentMethod;

    private LocalDateTime createdAt;
}
