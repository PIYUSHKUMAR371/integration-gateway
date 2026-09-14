package com.google.ae.integration_gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.ae.integration_gateway.dto.VendorWebhookPayloadDto;
import com.google.ae.integration_gateway.model.SystemAuditLog;
import com.google.ae.integration_gateway.repository.SystemAuditLogRepository;
import com.google.ae.integration_gateway.service.IntegrationGatewayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    private final IntegrationGatewayService gatewayService;
    private final SystemAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public WebhookController(IntegrationGatewayService gatewayService,
                             SystemAuditLogRepository auditLogRepository,
                             ObjectMapper objectMapper) {
        this.gatewayService = gatewayService;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/ingest")
    public ResponseEntity<String> ingestWebhook(
            @RequestHeader(value = "X-Signature-256", required = false) String signature,
            @RequestHeader(value = "X-Vendor-Secret", required = false) String sharedSecret,
            @RequestBody String rawPayloadJson) {

        try {
            VendorWebhookPayloadDto payloadDto = objectMapper.readValue(rawPayloadJson, VendorWebhookPayloadDto.class);
            payloadDto.setRawDataJson(rawPayloadJson);

            String result = gatewayService.processIncomingWebhook(rawPayloadJson, signature, sharedSecret, payloadDto);

            return switch (result) {
                case "PROCESSED" -> ResponseEntity.ok("Webhook processed successfully into canonical domain object.");
                case "DUPLICATE_SKIPPED" -> ResponseEntity.status(HttpStatus.OK).body("Event already processed (Idempotent Skip). Zero side-effects triggered.");
                case "INVALID_SIGNATURE" -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid HMAC signature. Request untrusted.");
                default -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to process webhook.");
            };
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON payload format: " + e.getMessage());
        }
    }

    @GetMapping("/logs")
    public ResponseEntity<List<SystemAuditLog>> getAuditLogs() {
        return ResponseEntity.ok(auditLogRepository.findAll());
    }
}