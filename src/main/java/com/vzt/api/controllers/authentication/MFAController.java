package com.vzt.api.controllers.authentication;

import com.vzt.api.dtos.authentication.MFAVerifyDTO;
import com.vzt.api.models.session.BrowserSession;
import com.vzt.api.models.session.SessionLogin;
import com.vzt.api.responses.ApplicationResponse;
import com.vzt.api.services.authentication.MFAService;
import com.vzt.api.services.authentication.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/authentication/multi-factor")
public class MFAController {
    private final MFAService mfaService;
    private final SessionService sessionService;

    /*@PostMapping("/verify")
    public ResponseEntity<ApplicationResponse<?>> verify(HttpServletRequest request, @RequestBody MFAVerifyDTO dto) {
        ApplicationResponse<?> response = mfaService.verifyMfa(dto, request);
        if(response.getData() instanceof BrowserSession browserSession){
            SessionLogin sessionLogin = browserSession.getLogins().get(0);
            return new ResponseEntity<>(
                new ApplicationResponse<>(
                        response.getStatus(),
                        response.getMessage(),
                        response.getTimestamp(),
                        sessionLogin.getAccessToken(),
                        null
                ),
                    response.getStatus().value
            );
        }
        if (response.getData() instanceof SessionLogin sessionLogin){
            
        }

    }*/


}
