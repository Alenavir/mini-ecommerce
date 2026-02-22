package ru.alenavir.mini_ecommerce.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.alenavir.mini_ecommerce.entity.Product;
import ru.alenavir.mini_ecommerce.repo.projection.ProductStockProjection;

import java.util.List;

public interface ProductRepo extends JpaRepository<Product, Long> {

    List<ProductStockProjection> findByIdIn(List<Long> ids);
}
