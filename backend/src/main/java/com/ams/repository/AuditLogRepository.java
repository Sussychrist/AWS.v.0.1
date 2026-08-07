package com.ams.repository;

import com.ams.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for AuditLog entity.
 * No delete or update methods - append-only by design.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    // No custom query methods - append-only repository
    // Save is inherited from JpaRepository
}
