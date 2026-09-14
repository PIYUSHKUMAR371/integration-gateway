package com.google.ae.integration_gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanonicalEventDto {
    private String internalEventId;
    private String idempotencyKey;
    private String sourceSystem;
    private String standardizedEventType;
    private String normalizedPayload;
    private LocalDateTime timestamp;
}