package ru.alenavir.mini_ecommerce.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_events")
@Data
public class ProcessedEvent {

    @Id
    private String eventId;

    private LocalDateTime processedAt;
}
