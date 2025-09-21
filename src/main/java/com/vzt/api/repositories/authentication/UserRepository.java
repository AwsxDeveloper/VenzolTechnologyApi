package com.vzt.api.repositories.authentication;

import com.vzt.api.models.authentication.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUid(UUID id);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByUid(UUID uuid);

    Optional<User> findByMailCode_Code(String code);
}
