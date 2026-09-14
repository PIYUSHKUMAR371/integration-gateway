package com.google.ae.integration_gateway.service;

import com.google.ae.integration_gateway.dto.CanonicalEventDto;
import com.google.ae.integration_gateway.dto.VendorWebhookPayloadDto;
import com.google.ae.integration_gateway.model.EventStatus;
import com.google.ae.integration_gateway.model.SystemAuditLog;
import com.google.ae.integration_gateway.repository.SystemAuditLogRepository;
import com.google.ae.integration_gateway.security.HmacValidator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class IntegrationGatewayService {

    private final HmacValidator hmacValidator;
    private final IdempotencyService idempotencyService;
    private final PayloadTransformerService transformerService;
    private final SystemAuditLogRepository auditLogRepository;

    public IntegrationGatewayService(HmacValidator hmacValidator,
                                      IdempotencyService idempotencyService,
                                      PayloadTransformerService transformerService,
                                      SystemAuditLogRepository auditLogRepository) {
        this.hmacValidator = hmacValidator;
        this.idempotencyService = idempotencyService;
        this.transformerService = transformerService;
        this.auditLogRepository = auditLogRepository;
    }

    public String processIncomingWebhook(String rawPayload, String signature, String sharedSecret, VendorWebhookPayloadDto payloadDto) {
        // 1. Cryptographic Security Signature Validation (HMAC SHA-256)
        if (sharedSecret != null && signature != null && !sharedSecret.isEmpty()) {
            boolean isValid = hmacValidator.isValidSignature(rawPayload, signature, sharedSecret);
            if (!isValid) {
                saveAuditLog(payloadDto.getEventId(), payloadDto.getVendor(), rawPayload, EventStatus.INVALID_SIGNATURE, "Cryptographic HMAC signature mismatch");
                return "INVALID_SIGNATURE";
            }
        }

        // 2. Idempotency Check (Prevent duplicate execution)
        if (idempotencyService.isDuplicate(payloadDto.getEventId())) {
            saveAuditLog(payloadDto.getEventId(), payloadDto.getVendor(), rawPayload, EventStatus.DUPLICATE_SKIPPED, "Idempotency key already processed. Skipped duplicate execution.");
            return "DUPLICATE_SKIPPED";
        }

        // 3. Payload Transformation (Enterprise Adapter Pattern)
        CanonicalEventDto canonicalEvent = transformerService.transformToCanonical(payloadDto);

        // 4. Record Event Idempotency & Save Audit Trail
        idempotencyService.recordEvent(payloadDto.getEventId(), payloadDto.getVendor(), canonicalEvent.getStandardizedEventType(), EventStatus.PROCESSED);
        saveAuditLog(payloadDto.getEventId(), payloadDto.getVendor(), canonicalEvent.toString(), EventStatus.PROCESSED, "Successfully validated, transformed, and processed");

        return "PROCESSED";
    }

    private void saveAuditLog(String idempotencyKey, String vendor, String payload, EventStatus status, String details) {
        SystemAuditLog log = SystemAuditLog.builder()
                .idempotencyKey(idempotencyKey != null ? idempotencyKey : "UNKNOWN")
                .sourceVendor(vendor != null ? vendor : "UNKNOWN")
                .transformedPayload(payload)
                .status(status)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);
    }
}