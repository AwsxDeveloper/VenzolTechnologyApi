package com.vzt.api.repositories.authentication;

import com.vzt.api.models.authentication.Language;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LanguageRepository extends JpaRepository<Language, Long> {
    Optional<Language> findByShortName(String shortName);
}
