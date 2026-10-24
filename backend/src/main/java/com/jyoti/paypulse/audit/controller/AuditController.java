package com.jyoti.paypulse.audit.controller;

import com.jyoti.paypulse.audit.model.AuditLog;
import com.jyoti.paypulse.audit.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@Tag(name = "Audit & Observability", description = "Distributed event logs and Correlation ID tracing")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @GetMapping("/logs")
    @Operation(summary = "Get recent distributed audit logs across all microservices")
    public ResponseEntity<List<AuditLog>> getRecentLogs() {
        return ResponseEntity.ok(auditService.getRecentLogs());
    }

    @GetMapping("/trace/{correlationId}")
    @Operation(summary = "Trace distributed request journey across microservices by Correlation ID")
    public ResponseEntity<List<AuditLog>> getTrace(@PathVariable String correlationId) {
        return ResponseEntity.ok(auditService.getLogsByCorrelationId(correlationId));
    }
}
