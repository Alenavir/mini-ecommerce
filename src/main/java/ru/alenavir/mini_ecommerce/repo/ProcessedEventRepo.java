package ru.alenavir.mini_ecommerce.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.alenavir.mini_ecommerce.entity.ProcessedEvent;

public interface ProcessedEventRepo extends JpaRepository<ProcessedEvent, String> {
    boolean existsById(String eventId);
}
