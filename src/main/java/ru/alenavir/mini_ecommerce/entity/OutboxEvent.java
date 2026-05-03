package ru.alenavir.mini_ecommerce.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Data
@NoArgsConstructor
public class OutboxEvent {

    @Id
    private UUID id;

    private String eventType;

    @Column(columnDefinition = "text")
    private String payload;       // JSON строка события

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;  // PENDING, SENT, FAILED

    private LocalDateTime createdAt;
    private int attempts;
    private LocalDateTime lastAttemptAt;

    public enum OutboxStatus {
        PENDING, SENT, FAILED
    }
}