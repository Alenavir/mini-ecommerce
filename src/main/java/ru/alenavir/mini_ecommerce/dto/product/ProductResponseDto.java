package ru.alenavir.mini_ecommerce.dto.product;

import lombok.Getter;
import lombok.Setter;
import ru.alenavir.mini_ecommerce.entity.enums.Category;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProductResponseDto implements Serializable {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantityInStock;
    private Category category;
    private String sku;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
