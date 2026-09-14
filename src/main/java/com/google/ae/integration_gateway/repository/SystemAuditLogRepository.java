package com.google.ae.integration_gateway.repository;

import com.google.ae.integration_gateway.model.SystemAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemAuditLogRepository extends JpaRepository<SystemAuditLog, Long> {
}