package ru.alenavir.mini_ecommerce.repo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.alenavir.mini_ecommerce.entity.Order;

import java.util.List;

public interface OrderRepo extends JpaRepository<Order, Long> {
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.items i " +
            "LEFT JOIN FETCH i.product")
    List<Order> findAllWithItemsAndProducts();

    boolean existsByIdAndUserId(Long id, Long userId);

    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.createdAt DESC, o.id DESC")
    List<Order> findLastOrdersByUserId(@Param("userId") Long userId, Pageable pageable);
}
