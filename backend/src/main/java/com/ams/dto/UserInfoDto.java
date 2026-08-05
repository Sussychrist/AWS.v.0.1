package com.ams.dto;

/**
 * User info DTO for login response and current user endpoint.
 * Does not include sensitive data like password or email.
 */
public record UserInfoDto(
    Long userId,
    String username,
    String fullName,
    String role
) {
    
    /**
     * Create UserInfoDto from entity.
     *
     * @param userId the user ID
     * @param username the username
     * @param fullName the full name
     * @param role the role (ADMIN or USER)
     */
    public UserInfoDto(Long userId, String username, String fullName, String role) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }
}
