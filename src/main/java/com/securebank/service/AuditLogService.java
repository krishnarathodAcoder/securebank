
package com.securebank.service;

import com.securebank.entity.AuditLog;
import com.securebank.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public AuditLog recordAction(
            UUID actorUserId,
            String action,
            String entityType,
            UUID entityId) {

        AuditLog auditLog = new AuditLog();
        auditLog.setActorUserId(actorUserId);
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);

        return auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }

    public List<AuditLog> getLogsByEntityType(String entityType) {
        return auditLogRepository
                .findByEntityTypeOrderByCreatedAtDesc(entityType);
    }

    public List<AuditLog> getLogsByActor(UUID actorUserId) {
        return auditLogRepository
                .findByActorUserIdOrderByCreatedAtDesc(actorUserId);
    }
}
