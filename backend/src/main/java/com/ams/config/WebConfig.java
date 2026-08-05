package com.ams.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * CORS configuration for frontend-backend communication.
 * Allows localhost:5173 for Vite React dev server.
 */
@Configuration
public class WebConfig {

    /**
     * CORS filter bean to enable cross-origin requests from frontend.
     *
     * @return CorsFilter instance
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Allow frontend development server and production domains
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedOriginPatterns(List.of("*"));
        
        // Allowed HTTP methods
        config.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));
        
        // Allowed headers
        config.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept"
        ));
        
        // Expose headers if needed
        config.setExposedHeaders(List.of("Authorization"));
        
        // Do not allow credentials (using Bearer token in header)
        config.setAllowCredentials(false);
        
        // Cache preflight response for 1 hour
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
