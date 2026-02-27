package ru.alenavir.mini_ecommerce.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.alenavir.mini_ecommerce.entity.Product;
import ru.alenavir.mini_ecommerce.entity.enums.Category;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepo extends JpaRepository<Product, Long> {

    @Query("""
       SELECT p FROM Product p
       WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
       AND (:sku IS NULL OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :sku, '%')))
       AND (:category IS NULL OR p.category = :category)
       AND (:minPrice IS NULL OR p.price >= :minPrice)
       AND (:maxPrice IS NULL OR p.price <= :maxPrice)
       """)
    List<Product> search(
            @Param("name") String name,
            @Param("sku") String sku,
            @Param("category") Category category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );
}
