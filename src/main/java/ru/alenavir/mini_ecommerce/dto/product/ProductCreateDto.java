package ru.alenavir.mini_ecommerce.dto.product;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import ru.alenavir.mini_ecommerce.entity.enums.Category;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductCreateDto {

    @NotBlank(message = "Name must not be blank")
    @Size(min = 2, max = 255)
    private String name;

    @Size(max = 500, message = "Description too long")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Quantity is required")
    @PositiveOrZero(message = "Quantity cannot be negative")
    private Integer quantityInStock;

    @NotNull(message = "Category must not be null")
    private Category category;

    @NotBlank(message = "SKU is required")
    private String sku;
}