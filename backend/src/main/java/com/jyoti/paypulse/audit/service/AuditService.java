package com.jyoti.paypulse.audit.service;

import com.jyoti.paypulse.audit.model.AuditLog;
import com.jyoti.paypulse.audit.repository.AuditLogRepository;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public AuditLog record(String serviceName, String action, String details, String level) {
        String correlationId = MDC.get("correlationId");
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        AuditLog log = new AuditLog(correlationId, serviceName, action, details, level);
        return auditLogRepository.save(log);
    }

    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop50ByOrderByTimestampDesc();
    }

    public List<AuditLog> getLogsByCorrelationId(String correlationId) {
        return auditLogRepository.findByCorrelationIdOrderByTimestampAsc(correlationId);
    }
}
