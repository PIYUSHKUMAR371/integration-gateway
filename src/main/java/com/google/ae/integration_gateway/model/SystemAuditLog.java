package com.google.ae.integration_gateway.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private String sourceVendor;

    @Column(length = 2000)
    private String transformedPayload; // Canonical internal JSON model

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    private String details;

    private LocalDateTime timestamp;
}