
package com.securebank.repository;

import com.securebank.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findByEntityTypeOrderByCreatedAtDesc(
            String entityType);

    List<AuditLog> findByActorUserIdOrderByCreatedAtDesc(
            UUID actorUserId);
}
