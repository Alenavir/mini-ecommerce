package ru.alenavir.mini_ecommerce.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "Информация о товаре в заказе")
public class OrderItemResponseDto implements Serializable {

    @Schema(description = "ID позиции заказа", example = "15")
    private Long id;

    @Schema(description = "ID продукта", example = "5")
    private Long productId;

    @Schema(description = "Название продукта", example = "iPhone 15")
    private String productName;

    @Schema(description = "Количество товара", example = "2")
    private Integer quantity;

    @Schema(description = "Цена за единицу товара", example = "999.99")
    private BigDecimal price;
}