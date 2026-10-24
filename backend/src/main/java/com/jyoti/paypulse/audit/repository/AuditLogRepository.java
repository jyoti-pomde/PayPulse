package com.jyoti.paypulse.audit.repository;

import com.jyoti.paypulse.audit.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop50ByOrderByTimestampDesc();
    List<AuditLog> findByCorrelationIdOrderByTimestampAsc(String correlationId);
}
