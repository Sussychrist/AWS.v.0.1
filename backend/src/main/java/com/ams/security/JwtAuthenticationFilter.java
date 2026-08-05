package com.ams.security;

import com.ams.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter that validates Bearer tokens on every request.
 * Sets the Spring Security context if token is valid.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            UserDetailsServiceImpl userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = getJwtFromRequest(request);

        if (StringUtils.hasText(token)) {
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    String username = jwtTokenProvider.getUsernameFromToken(token);
                    
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception ex) {
                // Token is invalid, continue without authentication
                // The security chain will handle unauthorized access
                logger.debug("Could not set user authentication from token", ex);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header.
     *
     * @param request the HTTP request
     * @return the token string or null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }
}
