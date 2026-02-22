package ru.alenavir.mini_ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductBatchUpdateService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void batchUpdateQuantities(Map<Long, Integer> updatedQuantities) {
        if (updatedQuantities.isEmpty()) return;

        // Формируем SQL CASE WHEN
        StringBuilder sql = new StringBuilder("UPDATE products SET quantity_in_stock = CASE id ");

        updatedQuantities.forEach((id, qty) -> {
            sql.append("WHEN ").append(id).append(" THEN ").append(qty).append(" ");
        });

        sql.append("END WHERE id IN (")
                .append(updatedQuantities.keySet().stream().map(String::valueOf).collect(Collectors.joining(",")))
                .append(")");

        jdbcTemplate.update(sql.toString());
    }
}
