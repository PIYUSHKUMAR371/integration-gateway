package com.google.ae.integration_gateway.service;

import com.google.ae.integration_gateway.model.EventStatus;
import com.google.ae.integration_gateway.model.ProcessedEvent;
import com.google.ae.integration_gateway.repository.ProcessedEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class IdempotencyService {

    private final ProcessedEventRepository processedEventRepository;

    public IdempotencyService(ProcessedEventRepository processedEventRepository) {
        this.processedEventRepository = processedEventRepository;
    }

    public boolean isDuplicate(String idempotencyKey) {
        return processedEventRepository.existsByIdempotencyKey(idempotencyKey);
    }

    @Transactional
    public ProcessedEvent recordEvent(String idempotencyKey, String vendor, String eventType, EventStatus status) {
        ProcessedEvent event = ProcessedEvent.builder()
                .idempotencyKey(idempotencyKey)
                .vendorName(vendor)
                .eventType(eventType)
                .status(status)
                .processedAt(LocalDateTime.now())
                .build();

        return processedEventRepository.save(event);
    }
}
