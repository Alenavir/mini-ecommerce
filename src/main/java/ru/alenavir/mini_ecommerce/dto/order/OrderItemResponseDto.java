package ru.alenavir.mini_ecommerce.dto.order;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemResponseDto {

    private Long id;
    private Long productId;
    private String productName;

    private Integer quantity;
    private BigDecimal price;
}
