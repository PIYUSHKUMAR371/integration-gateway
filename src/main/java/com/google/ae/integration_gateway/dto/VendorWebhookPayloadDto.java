package com.google.ae.integration_gateway.dto;

import lombok.Data;

@Data
public class VendorWebhookPayloadDto {
    private String eventId;          // e.g., "evt_stripe_998877"
    private String vendor;           // e.g., "STRIPE", "SALESFORCE", "GITHUB"
    private String eventType;        // e.g., "payment.succeeded", "user.created"
    private String rawDataJson;      // Vendor payload details
}