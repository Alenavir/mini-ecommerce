package ru.alenavir.mini_ecommerce.dto.product;

import lombok.Getter;
import lombok.Setter;
import ru.alenavir.mini_ecommerce.entity.enums.Category;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductSearchDto {

    private String name;
    private String sku;
    private Category category;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
