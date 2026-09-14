package com.google.ae.integration_gateway.service;

import com.google.ae.integration_gateway.dto.CanonicalEventDto;
import com.google.ae.integration_gateway.dto.VendorWebhookPayloadDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PayloadTransformerService {

    /**
     * Enterprise Adapter Pattern: Normalizes external heterogeneous vendor payloads
     * into a single canonical internal schema.
     */
    public CanonicalEventDto transformToCanonical(VendorWebhookPayloadDto vendorDto) {
        String standardizedEventType = mapVendorEventType(vendorDto.getVendor(), vendorDto.getEventType());

        return CanonicalEventDto.builder()
                .internalEventId("INT-EVT-" + UUID.randomUUID().toString().substring(0, 8))
                .idempotencyKey(vendorDto.getEventId())
                .sourceSystem(vendorDto.getVendor() != null ? vendorDto.getVendor().toUpperCase() : "UNKNOWN")
                .standardizedEventType(standardizedEventType)
                .normalizedPayload(vendorDto.getRawDataJson())
                .timestamp(LocalDateTime.now())
                .build();
    }

    private String mapVendorEventType(String vendor, String rawType) {
        if (vendor == null || rawType == null) return "GENERIC_EVENT";

        return switch (vendor.toUpperCase()) {
            case "STRIPE" -> rawType.contains("succeeded") ? "PAYMENT_COMPLETED" : "PAYMENT_FAILED";
            case "SALESFORCE" -> rawType.contains("created") ? "CUSTOMER_REGISTERED" : "CUSTOMER_UPDATED";
            case "GITHUB" -> rawType.contains("push") ? "CODE_DEPLOYED" : "WORKFLOW_TRIGGERED";
            default -> "GENERIC_VENDOR_EVENT";
        };
    }
}