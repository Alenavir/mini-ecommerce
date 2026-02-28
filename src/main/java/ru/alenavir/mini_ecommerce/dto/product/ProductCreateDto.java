package ru.alenavir.mini_ecommerce.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import ru.alenavir.mini_ecommerce.entity.enums.Category;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "DTO для создания продукта")
public class ProductCreateDto {

    @Schema(description = "Название продукта", example = "iPhone 15")
    @NotBlank
    @Size(min = 2, max = 255)
    private String name;

    @Schema(description = "Описание продукта", example = "Новый iPhone 15 с OLED экраном")
    @Size(max = 500)
    private String description;

    @Schema(description = "Цена продукта", example = "999.99")
    @NotNull
    @Positive
    private BigDecimal price;

    @Schema(description = "Количество на складе", example = "10")
    @NotNull
    @PositiveOrZero
    private Integer quantityInStock;

    @Schema(description = "Категория продукта", example = "SMARTPHONE")
    @NotNull
    private Category category;

    @Schema(description = "SKU продукта", example = "IP15-001")
    @NotBlank
    private String sku;
}