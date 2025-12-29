package com.vzt.api.repositories.realm;

import com.vzt.api.models.realm.OAuthCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthCodeRepository extends JpaRepository<OAuthCode, Long> {
    Optional<OAuthCode> findByCode(String code);
}
