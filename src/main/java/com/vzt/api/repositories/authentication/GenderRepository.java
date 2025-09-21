package com.vzt.api.repositories.authentication;

import com.vzt.api.models.authentication.Gender;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GenderRepository extends JpaRepository<Gender, Long> {
    Optional<Gender> findByName(String name);
}
