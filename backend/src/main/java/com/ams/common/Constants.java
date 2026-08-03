package com.ams.common;

public final class Constants {

    private Constants() {
        // Utility class
    }

    // User roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";

    // User status
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";

    // Abnormal priority
    public static final String PRIORITY_LOW = "LOW";
    public static final String PRIORITY_MEDIUM = "MEDIUM";
    public static final String PRIORITY_HIGH = "HIGH";

    // Abnormal status
    public static final String ABNORMAL_STATUS_OPEN = "OPEN";
    public static final String ABNORMAL_STATUS_PROCESSING = "PROCESSING";
    public static final String ABNORMAL_STATUS_CLOSED = "CLOSED";

    // Audit actions
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";

    // Image MIME types
    public static final String MIME_JPEG = "image/jpeg";
    public static final String MIME_PNG = "image/png";

    // Max image size (5MB)
    public static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

    // Max images per abnormal
    public static final int MAX_IMAGES_PER_ABNORMAL = 10;
}
