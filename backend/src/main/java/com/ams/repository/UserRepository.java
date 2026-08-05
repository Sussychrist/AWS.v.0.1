package com.ams.repository;

import com.ams.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for UserInfo entity.
 */
@Repository
public interface UserRepository extends JpaRepository<UserInfo, Long> {

    /**
     * Find user by username (case-insensitive).
     *
     * @param username the username to search
     * @return Optional containing the user if found
     */
    Optional<UserInfo> findByUsername(String username);

    /**
     * Check if a username exists (case-insensitive).
     *
     * @param username the username to check
     * @return true if username exists
     */
    boolean existsByUsername(String username);
}
