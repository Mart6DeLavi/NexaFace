package com.nextface.audit;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists audit records. Uses a new transaction so failed authentication
 * attempts are recorded even though the login transaction is rolled back.
 */
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(UUID userId, AuditEventType eventType, boolean success) {
        auditLogRepository.save(new AuditLog(userId, null, eventType, success));
    }
}
