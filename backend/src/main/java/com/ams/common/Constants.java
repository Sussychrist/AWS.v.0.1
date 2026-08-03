package com.ams.common;

/**
 * Application-wide constants.
 */
public final class Constants {
    
    private Constants() {
        // Prevent instantiation
    }
    
    // User Roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";
    
    // User Status
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    
    // Abnormal Status
    public static final String ABNORMAL_STATUS_OPEN = "OPEN";
    public static final String ABNORMAL_STATUS_PROCESSING = "PROCESSING";
    public static final String ABNORMAL_STATUS_CLOSED = "CLOSED";
    
    // Abnormal Priority
    public static final String PRIORITY_LOW = "LOW";
    public static final String PRIORITY_MEDIUM = "MEDIUM";
    public static final String PRIORITY_HIGH = "HIGH";
    
    // Audit Actions
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";
    
    // Entity Names for Audit
    public static final String ENTITY_ABNORMAL = "ABNORMAL";
    public static final String ENTITY_USER_INFO = "USER_INFO";
    public static final String ENTITY_PROCESS_STEP = "PROCESS_STEP";
    public static final String ENTITY_DEPARTMENT = "DEPARTMENT";
    public static final String ENTITY_ABNORMAL_IMAGE = "ABNORMAL_IMAGE";
    
    // Image Constraints
    public static final int MAX_IMAGES_PER_ABNORMAL = 10;
    public static final long MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024; // 5MB
    public static final String[] ALLOWED_IMAGE_MIME_TYPES = {"image/jpeg", "image/png"};
    
    // Password Constraints
    public static final int MIN_PASSWORD_LENGTH = 8;
    
    // JWT
    public static final long JWT_EXPIRATION_MS = 28800000; // 8 hours
    
    // Abnormal Number
    public static final int DAILY_ABNORMAL_LIMIT = 99999;
}
