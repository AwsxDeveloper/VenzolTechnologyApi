package com.vzt.api.config;

import com.vzt.api.models.authentication.User;
import com.vzt.api.models.security.Role;
import com.vzt.api.models.session.BrowserSession;
import com.vzt.api.models.session.SessionLogin;
import com.vzt.api.repositories.authentication.UserRepository;
import com.vzt.api.repositories.realm.RealmRepository;
import com.vzt.api.repositories.security.EndpointRepository;
import com.vzt.api.repositories.session.BrowserSessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Component
@RequiredArgsConstructor
public class CustomOncePerRequestFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final BrowserSessionRepository  browserSessionRepository;
    private final RealmRepository realmRepository;
    private final EndpointRepository endpointRepository;


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(7);
        username = jwtService.extractUsername(jwt);
        if (username != null&& SecurityContextHolder.getContext().getAuthentication() == null) {
            final Cookie[] cookies = request.getCookies();
            if (cookies == null) {
                filterChain.doFilter(request, response);
                return;
            }
            BrowserSession browserSession = null;
             for (Cookie cookie : cookies) {
                if (cookie.getName().equals("SESSION_ID")&&cookie.getValue() != null) {
                    Optional<BrowserSession> optionalBrowserSession = browserSessionRepository.findBySessionId(UUID.fromString(cookie.getValue()));
                    if (optionalBrowserSession.isPresent()) {
                        browserSession = optionalBrowserSession.get();
                    }
                }
            }
            if (browserSession == null) {
                filterChain.doFilter(request, response);
                return;
            }

            SessionLogin sessionLogin = null;
            for(SessionLogin login: browserSession.getLogins()){
                if (Objects.equals(login.getAccessToken(), jwt)) {
                    sessionLogin = login;
                }
            }

            if (sessionLogin == null) {
                filterChain.doFilter(request, response);
                return;
            }

            if(sessionLogin.getMfaToken() != null) {
                filterChain.doFilter(request, response);
                return;
            }


            Optional<User> optionalUser = userRepository.findByUsername(username);
            if (optionalUser.isEmpty()){
                filterChain.doFilter(request, response);
                return;
            }
            User user = optionalUser.get();

            Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
            for (Role authority : user.getAuthorities()) {
                authorities.add(new SimpleGrantedAuthority(authority.getName()));
            }



            if (jwtService.isTokenValid(jwt, user.getUsername())){
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        authorities
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }


        }

        filterChain.doFilter(request, response);
    }
}
