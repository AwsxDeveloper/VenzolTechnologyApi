package com.vzt.api.config;

import com.vzt.api.models.security.HttpMethod;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Component
@AllArgsConstructor
public class DynamicCorsConfigurationSource implements CorsConfigurationSource {
    private final AllowedOriginService allowedOriginService;

    @Override
    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
        String origin = request.getHeader(HttpHeaders.ORIGIN);
        List<String> defaultOrigins = Arrays.asList("http://localhost:4200","http://localhost:5000", "http://localhost:5555","http://localhost:55555","https://tsbackend-pr77.onrender.com","https://authvzt.pages.dev","https://venzolsprinkler.pages.dev");

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        if (origin==null){
            config.setAllowedOrigins(List.of());
        } else if (defaultOrigins.contains(origin)) {
            config.setAllowedOrigins(defaultOrigins);
        }else if(allowedOriginService.isAllowed(origin, request.getContextPath(), HttpMethod.valueOf(request.getMethod()))){
            config.setAllowedOrigins(List.of(origin));
        }else {
            config.setAllowedOrigins(List.of());
        }

        return config;

    }
}
