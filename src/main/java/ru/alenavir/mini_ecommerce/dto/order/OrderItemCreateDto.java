package ru.alenavir.mini_ecommerce.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Товар для добавления в заказ")
public class OrderItemCreateDto {

    @Schema(description = "ID продукта", example = "5")
    @NotNull
    private Long productId;

    @Schema(description = "Количество товара", example = "2")
    @Positive
    private Integer quantity;
}