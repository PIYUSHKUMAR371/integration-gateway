package com.google.ae.integration_gateway.repository;

import com.google.ae.integration_gateway.model.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {

    // Fast O(1) database index check for idempotency
    boolean existsByIdempotencyKey(String idempotencyKey);

    Optional<ProcessedEvent> findByIdempotencyKey(String idempotencyKey);
}