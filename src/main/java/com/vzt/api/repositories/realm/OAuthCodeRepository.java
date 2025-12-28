package com.vzt.api.repositories.realm;

import com.vzt.api.models.realm.OAuthCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthCodeRepository extends JpaRepository<OAuthCode, Long> {
}
