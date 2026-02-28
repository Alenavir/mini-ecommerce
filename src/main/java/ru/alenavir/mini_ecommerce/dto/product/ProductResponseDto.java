package ru.alenavir.mini_ecommerce.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import ru.alenavir.mini_ecommerce.entity.enums.Category;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "DTO с информацией о продукте")
public class ProductResponseDto implements Serializable {

    @Schema(description = "ID продукта", example = "1")
    private Long id;

    @Schema(description = "Название продукта", example = "iPhone 15")
    private String name;

    @Schema(description = "Описание продукта", example = "Новый iPhone 15 с OLED экраном")
    private String description;

    @Schema(description = "Цена продукта", example = "999.99")
    private BigDecimal price;

    @Schema(description = "Количество на складе", example = "10")
    private Integer quantityInStock;

    @Schema(description = "Категория продукта", example = "SMARTPHONE")
    private Category category;

    @Schema(description = "SKU продукта", example = "IP15-001")
    private String sku;

    @Schema(description = "Дата создания продукта", example = "2026-02-28T10:15:30")
    private LocalDateTime createdAt;

    @Schema(description = "Дата последнего обновления продукта", example = "2026-02-28T12:30:00")
    private LocalDateTime updatedAt;
}
