package com.vzt.api.controllers.realm;

import com.vzt.api.dtos.realm.OAuthCodeCreateDTO;
import com.vzt.api.responses.ApplicationResponse;
import com.vzt.api.services.realm.OAuthRedirectService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/oauth")
public class OAuthController {

    private final OAuthRedirectService  redirectService;

    @PostMapping("/code")
    public ResponseEntity<ApplicationResponse<String>> createCode(HttpServletRequest request, @RequestBody OAuthCodeCreateDTO dto) {
        ApplicationResponse<String> response = redirectService.create(request, dto);
        return ResponseEntity.status(response.getStatus().value).body(response);
    }

}
