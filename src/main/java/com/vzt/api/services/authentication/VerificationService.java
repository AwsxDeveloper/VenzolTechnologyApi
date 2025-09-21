package com.vzt.api.services.authentication;

import com.vzt.api.config.JwtService;
import com.vzt.api.models.authentication.User;
import com.vzt.api.models.session.BrowserSession;
import com.vzt.api.models.session.SessionLogin;
import com.vzt.api.repositories.authentication.UserRepository;
import com.vzt.api.repositories.session.BrowserSessionRepository;
import com.vzt.api.responses.ApplicationResponse;
import com.vzt.api.responses.ResponseStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final SessionService sessionService;
    private final BrowserSessionRepository browserSessionRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public ApplicationResponse<?> verify(HttpServletRequest request) {
        String sessionId = sessionService.getSessionId(request);
        if (sessionId == null) {
            return new ApplicationResponse<>(ResponseStatus.UNAUTHORIZED, "Empty session!", LocalDateTime.now(), null, null);
        }

        Optional<BrowserSession> browserSessionOptional = browserSessionRepository.findBySessionId(UUID.fromString(sessionId));
        if (browserSessionOptional.isEmpty()){
            return new ApplicationResponse<>(ResponseStatus.UNAUTHORIZED, "Session not found!", LocalDateTime.now(), null, null);
        }

        int activeUserId = request.getIntHeader("Active-User");

        BrowserSession browserSession = browserSessionOptional.get();

        SessionLogin sessionLogin = browserSession.getLogins().get(activeUserId);
        if (sessionLogin == null) {
            return new ApplicationResponse<>(ResponseStatus.UNAUTHORIZED, "Login not found!", LocalDateTime.now(), null, null);
        }


        String accessToken = jwtService.generateAccessToken(sessionLogin.getUser());

        browserSession.getLogins().get(activeUserId).setAccessToken(accessToken);

        browserSessionRepository.save(browserSession);
        return new ApplicationResponse<>(ResponseStatus.SUCCESS, "Session verified!", LocalDateTime.now(), accessToken, null);

    };


}
