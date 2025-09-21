package com.vzt.api.config;

import com.vzt.api.models.security.Role;
import com.vzt.api.repositories.authentication.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    @Cacheable("roles")
    public List<Role> getRoles() {
        return roleRepository.findAll();
    }
}
