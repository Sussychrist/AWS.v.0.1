package com.ams.dto;

/**
 * Login response DTO containing JWT token and user info.
 */
public record LoginResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    UserInfoDto user
) {
    
    /**
     * Create LoginResponse.
     *
     * @param accessToken the JWT access token
     * @param tokenType the token type (Bearer)
     * @param expiresIn expiration time in seconds
     * @param user the user information
     */
    public LoginResponse(String accessToken, String tokenType, long expiresIn, UserInfoDto user) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.user = user;
    }
}
