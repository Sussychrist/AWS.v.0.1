package com.ams.service;

import com.ams.entity.UserInfo;
import com.ams.entity.UserRole;
import com.ams.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom UserDetailsService implementation for Spring Security.
 * Loads user from USER_INFO table by username.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo userInfo = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Create Spring Security User with authorities
        String role = "ROLE_" + userInfo.getRole().name();
        return new User(
                userInfo.getUsername(),
                userInfo.getPasswordHash(),
                java.util.Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}
