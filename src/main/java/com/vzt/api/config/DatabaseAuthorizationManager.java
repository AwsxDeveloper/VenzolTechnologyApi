package com.vzt.api.config;

import com.vzt.api.models.security.Endpoint;
import com.vzt.api.models.security.HttpMethod;
import com.vzt.api.models.security.Role;
import com.vzt.api.repositories.authentication.RoleRepository;
import com.vzt.api.repositories.authentication.UserRepository;
import com.vzt.api.repositories.security.EndpointRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Component
@AllArgsConstructor
public class DatabaseAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    private final EndpointRepository endpointRepository;
    private final RoleService roleService;

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        String requestPath = context.getRequest().getRequestURI();
        HttpMethod httpMethod = HttpMethod.valueOf(context.getRequest().getMethod());

        Optional<Endpoint> endpointOptional = endpointRepository.findEndpointByPathAndMethod(requestPath, httpMethod);
        if (endpointOptional.isEmpty()) {
            return new AuthorizationDecision(false);
        }

        Endpoint endpoint = endpointOptional.get();

        if (endpoint.isDisabled()){
            return new AuthorizationDecision(false);
        }

        if (endpoint.getPermission() == null) {
            return new AuthorizationDecision(true);
        }


        Authentication auth = authentication.get();

        if (auth == null||!auth.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }


        final List<Role> roles = roleService.getRoles();

        for (Role role : roles) {
            if (auth.getAuthorities().contains(new SimpleGrantedAuthority(role.getName()))&&role.getPermissions().contains(endpoint.getPermission())) {
                return new AuthorizationDecision(true);
            }
        }

        return new AuthorizationDecision(false);
    }

}
