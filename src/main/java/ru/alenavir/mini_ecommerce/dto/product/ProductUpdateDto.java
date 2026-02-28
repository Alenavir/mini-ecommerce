package ru.alenavir.mini_ecommerce.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.alenavir.mini_ecommerce.entity.enums.Category;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "DTO для обновления продукта")
public class ProductUpdateDto {

    @Schema(description = "Новое название продукта", example = "iPhone 15 Pro")
    @Size(min = 2, max = 255)
    private String name;

    @Schema(description = "Новое описание продукта", example = "iPhone 15 Pro с улучшенной камерой")
    @Size(max = 500)
    private String description;

    @Schema(description = "Новая цена продукта", example = "1099.99")
    @Positive
    private BigDecimal price;

    @Schema(description = "Новое количество на складе", example = "20")
    @PositiveOrZero
    private Integer quantityInStock;

    @Schema(description = "Новая категория продукта", example = "SMARTPHONE")
    private Category category;
}
