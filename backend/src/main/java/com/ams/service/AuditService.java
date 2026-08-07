package com.ams.service;

import com.ams.entity.AuditLog;
import com.ams.entity.UserRole;
import com.ams.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for recording audit logs.
 * Writes in the same transaction as the caller (propagation = REQUIRED).
 * Append-only: no update or delete methods.
 */
@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest request;

    /**
     * Constructor with dependency injection.
     *
     * @param auditLogRepository the audit log repository
     * @param request the HTTP request for IP extraction
     */
    public AuditService(AuditLogRepository auditLogRepository, HttpServletRequest request) {
        this.auditLogRepository = auditLogRepository;
        this.request = request;
    }

    /**
     * Record an audit log entry.
     * Resolves current user from SecurityContext and client IP from request.
     * Writes in the same transaction as the caller.
     *
     * @param action the action type (CREATE, UPDATE, DELETE)
     * @param entityName the name of the entity affected
     * @param entityId the numeric ID of the entity (nullable)
     * @param entityNo the business identifier of the entity (e.g., ABNORMAL_NO, nullable)
     * @param description human-readable description of the action
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void record(String action, String entityName, Long entityId, String entityNo, String description) {
        try {
            // Resolve current user from SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = null;
            String username = null;

            if (authentication != null && authentication.isAuthenticated() 
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                
                Object principal = authentication.getPrincipal();
                if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
                    username = userDetails.getUsername();
                    // Extract user ID from token or lookup - for now use placeholder
                    // In real implementation, would decode JWT to get userId claim
                    userId = resolveUserIdFromUsername(username);
                }
            }

            // If no authenticated user, use system user (ID 1 = admin)
            if (userId == null) {
                userId = 1L;
                username = "system";
            }

            // Get client IP address
            String ipAddress = getClientIpAddress();

            // Create and save audit log
            AuditLog auditLog = new AuditLog(
                userId,
                action,
                entityName,
                entityId,
                entityNo,
                description,
                LocalDateTime.now(),
                ipAddress
            );

            auditLogRepository.save(auditLog);
            logger.debug("Audit log recorded: {} by {} on {}", action, username, entityName);

        } catch (Exception e) {
            // Log error but don't throw - audit failure shouldn't break main transaction
            // However, per BR-AUD-003, audit is part of same transaction, so rethrow
            logger.error("Failed to record audit log", e);
            throw e;
        }
    }

    /**
     * Resolve user ID from username.
     * This is a simplified implementation - in production would use UserRepository.
     *
     * @param username the username
     * @return the user ID or null if not found
     */
    private Long resolveUserIdFromUsername(String username) {
        // Placeholder - would normally query database
        // For MVP, return null and let it default to system user
        return null;
    }

    /**
     * Get client IP address from HTTP request.
     * Checks X-Forwarded-For header first (for proxied requests), then remote address.
     *
     * @return the client IP address
     */
    private String getClientIpAddress() {
        if (request == null) {
            return "unknown";
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs; first one is the client
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
