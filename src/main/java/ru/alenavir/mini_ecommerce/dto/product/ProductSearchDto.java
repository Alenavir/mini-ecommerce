package ru.alenavir.mini_ecommerce.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import ru.alenavir.mini_ecommerce.entity.enums.Category;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "DTO для поиска продуктов с фильтрацией")
public class ProductSearchDto {

    @Schema(description = "Название продукта для поиска", example = "iPhone")
    private String name;

    @Schema(description = "SKU продукта", example = "IP15-001")
    private String sku;

    @Schema(description = "Категория продукта", example = "SMARTPHONE")
    private Category category;

    @Schema(description = "Минимальная цена", example = "500")
    private BigDecimal minPrice;

    @Schema(description = "Максимальная цена", example = "1500")
    private BigDecimal maxPrice;
}
