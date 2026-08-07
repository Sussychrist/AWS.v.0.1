package com.ams.dto;

import java.time.LocalDateTime;

/**
 * DTO for audit log data.
 * 
 * @param auditId the audit log ID
 * @param userId the user ID who performed the action
 * @param username the username (resolved separately)
 * @param action the action type (CREATE, UPDATE, DELETE)
 * @param entityName the entity name affected
 * @param entityId the entity numeric ID
 * @param entityNo the entity business identifier
 * @param description the action description
 * @param actionTime the timestamp of the action
 * @param ipAddress the client IP address
 */
public record AuditLogDto(
    Long auditId,
    Long userId,
    String username,
    String action,
    String entityName,
    Long entityId,
    String entityNo,
    String description,
    LocalDateTime actionTime,
    String ipAddress
) {
}
