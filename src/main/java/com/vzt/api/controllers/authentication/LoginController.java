package com.vzt.api.controllers.authentication;

import com.vzt.api.dtos.authentication.AuthenticationCheckDTO;
import com.vzt.api.dtos.authentication.UsernamePasswordDTO;
import com.vzt.api.models.session.BrowserSession;
import com.vzt.api.models.session.SessionLogin;
import com.vzt.api.responses.ApplicationResponse;
import com.vzt.api.responses.ResponseStatus;
import com.vzt.api.responses.authentication.LoginResponse;
import com.vzt.api.services.authentication.LoginService;
import com.vzt.api.services.authentication.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/authentication/login")
public class LoginController {

    private final LoginService loginService;
    private final SessionService sessionService;

    @PostMapping("/check")
    public ResponseEntity<ApplicationResponse<?>> checkUsernameOrEmail(HttpServletRequest request, @RequestBody AuthenticationCheckDTO dto){
        ApplicationResponse<?> response = loginService.checkUsernameOrEmail(request, dto);
        return new ResponseEntity<>(response, response.getStatus().value);
    }

    @PostMapping("")
    public ResponseEntity<ApplicationResponse<?>> login(HttpServletRequest request, @RequestBody UsernamePasswordDTO dto){
        ApplicationResponse<?> response = loginService.login(request,dto);
        if (response.getStatus()!= ResponseStatus.SUCCESS){
            return new ResponseEntity<>(response, response.getStatus().value);
        }

        if (response.getData() instanceof BrowserSession){
            SessionLogin sessionLogin = ((BrowserSession) response.getData()).getLogins().get(0);
            LoginResponse loginResponse = new LoginResponse(
                    sessionLogin.getMfaToken()!=null,
                    sessionService.isCredentialExpired(sessionLogin),
                    0
            );
            HttpHeaders headers = new HttpHeaders();
            headers.add("Set-Cookie", sessionService.createSessionIdCookie(String.valueOf(((BrowserSession) response.getData()).getSessionId())).toString());
            if(sessionLogin.getMfaToken()!=null){
                headers.add("Set-Cookie", sessionService.createMFACookie(sessionLogin.getMfaToken()).toString());
            }
            return new ResponseEntity<>(new ApplicationResponse<>(response.getStatus(), response.getMessage(), response.getTimestamp(), sessionLogin.getAccessToken(), loginResponse), headers,response.getStatus().value);
        }

        if (response.getData() instanceof SessionLogin sessionLogin){
            LoginResponse loginResponse = new LoginResponse(
                    sessionLogin.getMfaToken()!=null,
                    sessionService.isCredentialExpired(sessionLogin),
                    sessionService.getActiveUserId(sessionLogin)
            );
            HttpHeaders headers = new HttpHeaders();
            if(sessionLogin.getMfaToken()!=null){
                headers.add("Set-Cookie", sessionService.createMFACookie(sessionLogin.getMfaToken()).toString());
            }
            return new ResponseEntity<>(new ApplicationResponse<>(response.getStatus(), response.getMessage(), response.getTimestamp(), sessionLogin.getAccessToken(), loginResponse), headers, response.getStatus().value);
        }

        return new ResponseEntity<>(new ApplicationResponse<>(ResponseStatus.ERROR, "Bad request!", LocalDateTime.now(), null, null), HttpStatus.BAD_REQUEST);
    }

}
