package com.vzt.api.controllers.authentication;

import com.vzt.api.dtos.authentication.AuthenticationCheckDTO;
import com.vzt.api.dtos.authentication.EmailConfirmationDTO;
import com.vzt.api.dtos.authentication.RegisterDTO;
import com.vzt.api.models.session.BrowserSession;
import com.vzt.api.models.session.SessionLogin;
import com.vzt.api.responses.ApplicationResponse;
import com.vzt.api.responses.ResponseStatus;
import com.vzt.api.services.authentication.RegisterService;
import com.vzt.api.services.authentication.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/authentication/register")
public class RegisterController {
    private final RegisterService registerService;
    private final SessionService sessionService;

    @PostMapping("/check/email")
    public ResponseEntity<ApplicationResponse<?>> checkEmail(@RequestBody AuthenticationCheckDTO dto){
        ApplicationResponse<?> response = registerService.checkEmail(dto);
        return ResponseEntity.status(response.getStatus().value).body(response);
    }

    @PostMapping("/check/username")
    public ResponseEntity<ApplicationResponse<?>> checkUsername(@RequestBody AuthenticationCheckDTO dto){
        ApplicationResponse<?> response = registerService.checkUsername(dto);
        return ResponseEntity.status(response.getStatus().value).body(response);
    }

    @PostMapping("/check/phone-number")
    public ResponseEntity<ApplicationResponse<?>> checkPhoneNumber(@RequestBody AuthenticationCheckDTO dto){
        ApplicationResponse<?> response = registerService.checkPhoneNumber(dto);
        return ResponseEntity.status(response.getStatus().value).body(response);
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse<?>> register(@RequestBody RegisterDTO dto){
        ApplicationResponse<?> response = registerService.register(dto);
        return ResponseEntity.status(response.getStatus().value).body(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApplicationResponse<?>> confirm(HttpServletRequest request, @RequestBody EmailConfirmationDTO dto){
        ApplicationResponse<?> response = registerService.confirmRegistration(request,dto);
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(response.getStatus().value);
        if (response.getData() instanceof BrowserSession){
            BrowserSession browserSession = (BrowserSession) response.getData();
            builder.header("Set-Cookie", sessionService.createSessionIdCookie(browserSession.getSessionId().toString()).toString());
        }
        response.setData(null);
        return builder.body(response);
    }


}
