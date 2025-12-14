package com.vzt.api.repositories.session;

import com.vzt.api.models.session.SessionLogin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SessionLoginRepository extends JpaRepository<SessionLogin,Long> {

    boolean existsByLoginId(UUID loginId);
    boolean existsByMfaToken(String mfaToken);
}
