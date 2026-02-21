package ru.alenavir.mini_ecommerce.dto.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemCreateDto {

    @NotNull
    private Long productId;

    @Positive(message = "Quantity must be positive")
    private Integer quantity;

}
