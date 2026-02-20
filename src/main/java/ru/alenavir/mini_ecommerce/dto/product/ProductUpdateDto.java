package ru.alenavir.mini_ecommerce.dto.product;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.alenavir.mini_ecommerce.entity.enums.Category;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductUpdateDto {

    @Size(min = 2, max = 255)
    private String name;

    @Size(max = 500)
    private String description;

    @Positive
    private BigDecimal price;

    @PositiveOrZero
    private Integer quantityInStock;

    private Category category;
}
