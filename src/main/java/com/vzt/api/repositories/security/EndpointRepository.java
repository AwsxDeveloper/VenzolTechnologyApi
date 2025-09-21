package com.vzt.api.repositories.security;

import com.vzt.api.models.security.Endpoint;
import com.vzt.api.models.security.HttpMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EndpointRepository extends JpaRepository<Endpoint, Long> {
    Optional<Endpoint> findEndpointByPathAndMethod(String path, HttpMethod method);
    boolean getUsableForOAuthByPathAndMethod(String path, HttpMethod method);
    boolean existsByPathAndMethodAndDisabledIsFalse(String path, HttpMethod method);
}
