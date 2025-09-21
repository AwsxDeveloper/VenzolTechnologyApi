package com.vzt.api.services;

import com.vzt.api.config.JwtService;
import com.vzt.api.models.authentication.User;
import com.vzt.api.repositories.authentication.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserByRequestService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public User get(HttpServletRequest request){
        String jwt = request.getHeader(HttpHeaders.AUTHORIZATION).substring(7);
        String username = jwtService.extractUsername(jwt);
        Optional<User> userOptional = userRepository.findByUsername(username);
        return userOptional.orElse(null);
    }
}
