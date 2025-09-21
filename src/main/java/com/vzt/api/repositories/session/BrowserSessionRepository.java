package com.vzt.api.repositories.session;

import com.vzt.api.models.authentication.User;
import com.vzt.api.models.session.BrowserSession;
import com.vzt.api.models.session.SessionLogin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BrowserSessionRepository extends JpaRepository<BrowserSession, Long> {

    boolean existsBySessionId(UUID sessionId);

    Optional<BrowserSession> findByLoginsContains(SessionLogin login);
    Optional<BrowserSession> findBySessionId(UUID sessionId);

    Optional<BrowserSession> findByLogins_MfaToken(String mfaToken);
}
