package com.vzt.api.controllers.authentication;

import com.vzt.api.responses.ApplicationResponse;
import com.vzt.api.services.authentication.VerificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/authentication/verify")
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping()
    public ResponseEntity<ApplicationResponse<?>> verify(HttpServletRequest request) {
        ApplicationResponse<?> response = verificationService.verify(request);
        return new ResponseEntity<>(response, response.getStatus().value);
    }

}
