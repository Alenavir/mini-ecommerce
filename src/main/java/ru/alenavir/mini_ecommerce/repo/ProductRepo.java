package ru.alenavir.mini_ecommerce.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.alenavir.mini_ecommerce.entity.Product;

public interface ProductRepo extends JpaRepository<Product, Long> {
}
