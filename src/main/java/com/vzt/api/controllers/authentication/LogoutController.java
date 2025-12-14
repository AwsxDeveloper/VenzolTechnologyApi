package com.vzt.api.controllers.authentication;

import com.vzt.api.responses.ApplicationResponse;
import com.vzt.api.services.authentication.LogoutService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/authentication/logout")
public class LogoutController {

    private final LogoutService logoutService;

    @PostMapping("")
    public ResponseEntity<ApplicationResponse<?>> logout(HttpServletRequest request) {
        ApplicationResponse<?> response =  logoutService.logout(request);
        return new ResponseEntity<>(response, response.getStatus().value);
    }
}
