package com.ams.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Audit log entity mapped to AUDIT_LOG table.
 * Append-only: no update or delete operations.
 */
@Entity
@Table(name = "AUDIT_LOG")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_log_seq")
    @SequenceGenerator(name = "audit_log_seq", sequenceName = "SEQ_AUDIT_LOG", allocationSize = 1)
    @Column(name = "AUDIT_ID")
    private Long auditId;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "ACTION", nullable = false, length = 30)
    private String action;

    @Column(name = "ENTITY_NAME", nullable = false, length = 100)
    private String entityName;

    @Column(name = "ENTITY_ID")
    private Long entityId;

    @Column(name = "ENTITY_NO", length = 50)
    private String entityNo;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Column(name = "ACTION_TIME", nullable = false)
    private LocalDateTime actionTime;

    @Column(name = "IP_ADDRESS", length = 50)
    private String ipAddress;

    /**
     * Default constructor required by JPA.
     */
    public AuditLog() {
    }

    /**
     * Create an audit log entry.
     *
     * @param userId the ID of the user who performed the action
     * @param action the action type (CREATE, UPDATE, DELETE)
     * @param entityName the name of the entity affected
     * @param entityId the numeric ID of the entity
     * @param entityNo the business identifier of the entity (e.g., ABNORMAL_NO)
     * @param description human-readable description of the action
     * @param actionTime the timestamp of the action
     * @param ipAddress the client IP address
     */
    public AuditLog(Long userId, String action, String entityName, Long entityId,
                    String entityNo, String description, LocalDateTime actionTime, String ipAddress) {
        this.userId = userId;
        this.action = action;
        this.entityName = entityName;
        this.entityId = entityId;
        this.entityNo = entityNo;
        this.description = description;
        this.actionTime = actionTime;
        this.ipAddress = ipAddress;
    }

    // Getters and Setters

    public Long getAuditId() {
        return auditId;
    }

    public void setAuditId(Long auditId) {
        this.auditId = auditId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getEntityNo() {
        return entityNo;
    }

    public void setEntityNo(String entityNo) {
        this.entityNo = entityNo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getActionTime() {
        return actionTime;
    }

    public void setActionTime(LocalDateTime actionTime) {
        this.actionTime = actionTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
