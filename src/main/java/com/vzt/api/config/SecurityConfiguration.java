package com.vzt.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final CustomOncePerRequestFilter customOncePerRequestFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   DynamicCorsConfigurationSource corsSource,
                                                   DatabaseAuthorizationManager dbAuthManager
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable);
        http
                .cors(cors -> cors.configurationSource(corsSource));
        http
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers(HttpMethod.OPTIONS).permitAll()
                                .anyRequest().access(dbAuthManager));
        http
                .exceptionHandling(ex ->
                        ex
                            .accessDeniedHandler(new CustomAccessDeniedHandler())
        );
        http
                .sessionManagement((session) ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );
        http
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(customOncePerRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}