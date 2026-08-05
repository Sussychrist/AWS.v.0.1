package com.ams.controller;

import com.ams.common.ApiResponse;
import com.ams.dto.LoginRequest;
import com.ams.dto.LoginResponse;
import com.ams.dto.UserInfoDto;
import com.ams.entity.UserInfo;
import com.ams.repository.UserRepository;
import com.ams.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication controller for login, logout, and current user endpoints.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Login endpoint.
     * Validates credentials and returns JWT token.
     *
     * @param request the login request with username and password
     * @return login response with access token
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        // Find user by username
        UserInfo userInfo = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        // Check if user is active (BR-AUTH-001)
        if (userInfo.getStatus().name().equals("INACTIVE")) {
            throw new BadCredentialsException("Account is inactive. Please contact administrator.");
        }

        // Verify password
        if (!passwordEncoder.matches(request.password(), userInfo.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        // Generate JWT token (8 hours = 28800 seconds)
        String token = jwtTokenProvider.generateToken(
                userInfo.getUsername(),
                userInfo.getUserId(),
                userInfo.getRole().name()
        );

        // Create user info DTO (exclude sensitive data)
        UserInfoDto userDto = new UserInfoDto(
                userInfo.getUserId(),
                userInfo.getUsername(),
                userInfo.getFullName(),
                userInfo.getRole().name()
        );

        // Create login response
        LoginResponse response = new LoginResponse(
                token,
                "Bearer",
                28800L, // 8 hours in seconds
                userDto
        );

        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * Get current user info from JWT token.
     *
     * @return current user information
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoDto>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadCredentialsException("Not authenticated");
        }

        String username = authentication.getName();
        
        UserInfo userInfo = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        UserInfoDto userDto = new UserInfoDto(
                userInfo.getUserId(),
                userInfo.getUsername(),
                userInfo.getFullName(),
                userInfo.getRole().name()
        );

        return ResponseEntity.ok(ApiResponse.success("Success", userDto));
    }

    /**
     * Logout endpoint.
     * Stateless implementation - client removes token.
     *
     * @return success response
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // Stateless logout - client is responsible for removing the token
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }
}
