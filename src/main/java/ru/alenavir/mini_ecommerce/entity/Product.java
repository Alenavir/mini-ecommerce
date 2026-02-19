package ru.alenavir.mini_ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.alenavir.mini_ecommerce.entity.enums.Category;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Описание товара
    private String description;

    // Цена товара
    @Column(nullable = false)
    private BigDecimal price;

    // Количество на складе
    @Column(nullable = false)
    private Integer quantityInStock;

    // Категория товара
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    // артикул
    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
