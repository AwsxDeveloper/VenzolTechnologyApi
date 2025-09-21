package com.vzt.api.config;

import com.vzt.api.models.security.HttpMethod;
import com.vzt.api.repositories.realm.RealmRepository;
import com.vzt.api.repositories.security.EndpointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AllowedOriginService {
    private final RealmRepository realmRepository;
    private final EndpointRepository endpointRepository;


    @Cacheable("allowedOrigins")
    public boolean isAllowed(String origin, String path, HttpMethod method) {
        return realmRepository.existsByOriginAndDisabledIsFalse(origin)&& endpointRepository.getUsableForOAuthByPathAndMethod(path, method);
    }
}
