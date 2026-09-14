package com.google.ae.integration_gateway.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_events", indexes = {
    @Index(name = "idx_idempotency_key", columnList = "idempotencyKey", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique key sent by third-party vendor (e.g. evt_tx_998877)
    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private String vendorName; // e.g., "STRIPE", "SALESFORCE", "GITHUB"

    @Column(nullable = false)
    private String eventType; // e.g., "PAYMENT_SUCCESS", "USER_CREATED"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    private LocalDateTime processedAt;
}