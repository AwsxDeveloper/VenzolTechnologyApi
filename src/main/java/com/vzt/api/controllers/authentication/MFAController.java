package com.vzt.api.controllers.authentication;

import com.google.zxing.WriterException;
import com.vzt.api.dtos.account.DeleteMfaDTO;
import com.vzt.api.dtos.authentication.MFAVerifyDTO;
import com.vzt.api.models.session.BrowserSession;
import com.vzt.api.models.session.SessionLogin;
import com.vzt.api.responses.ApplicationResponse;
import com.vzt.api.responses.ResponseStatus;
import com.vzt.api.responses.account.HasMFAResponse;
import com.vzt.api.responses.authentication.LoginResponse;
import com.vzt.api.responses.authentication.MFAResponse;
import com.vzt.api.services.authentication.MFAService;
import com.vzt.api.services.authentication.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/authentication/multi-factor")
public class MFAController {
    private final MFAService mfaService;
    private final SessionService sessionService;

    @PostMapping("/verify")
    public ResponseEntity<ApplicationResponse<?>> verify(HttpServletRequest request, @RequestBody MFAVerifyDTO dto) {
        ApplicationResponse<?> response = mfaService.verifyMfa(dto, request);
        if (response.getData() instanceof BrowserSession browserSession){
            SessionLogin sessionLogin =  browserSession.getLogins().get(0);
            LoginResponse loginResponse = new LoginResponse(
                    sessionLogin.getMfaToken()!=null,
                    sessionService.isCredentialExpired(sessionLogin),
                    0
            );
            HttpHeaders headers = new HttpHeaders();
            headers.add("Set-Cookie", sessionService.createSessionIdCookie(String.valueOf(((BrowserSession) response.getData()).getSessionId())).toString());

            return new ResponseEntity<>(new ApplicationResponse<>(response.getStatus(), response.getMessage(), response.getTimestamp(), sessionLogin.getAccessToken(), loginResponse), headers,response.getStatus().value);
        }

        if (response.getData() instanceof SessionLogin sessionLogin){
            LoginResponse loginResponse = new LoginResponse(
                    sessionLogin.getMfaToken()!=null,
                    sessionService.isCredentialExpired(sessionLogin),
                    sessionService.getActiveUserId(sessionLogin)
            );
            HttpHeaders headers = new HttpHeaders();

            return new ResponseEntity<>(new ApplicationResponse<>(response.getStatus(), response.getMessage(), response.getTimestamp(), sessionLogin.getAccessToken(), loginResponse), headers, response.getStatus().value);
        }

        return new ResponseEntity<>(
                new ApplicationResponse<>(
                        response.getStatus(),
                        response.getMessage(),
                        response.getTimestamp(),
                        null,
                        null
                ),
                ResponseStatus.ERROR.value
        );
    }

    @PostMapping("/setup")
    public ResponseEntity<ApplicationResponse<MFAResponse>> setup(HttpServletRequest request) throws IOException, WriterException {
        ApplicationResponse<MFAResponse> response = mfaService.setMfa(request);
        return new ResponseEntity<>(response, response.getStatus().value);
    }

    @GetMapping("/status")
    public ResponseEntity<ApplicationResponse<HasMFAResponse>> status(HttpServletRequest request) {
        ApplicationResponse<HasMFAResponse> response = mfaService.hasMfA(request);
        return new ResponseEntity<>(response, response.getStatus().value);
    }

    @PostMapping("/delete")
    public ResponseEntity<ApplicationResponse<?>> delete(HttpServletRequest request, @RequestBody DeleteMfaDTO dto) {
        ApplicationResponse<?> response = mfaService.deleteMFA(request, dto);
        return new ResponseEntity<>(response, response.getStatus().value);
    }
}
