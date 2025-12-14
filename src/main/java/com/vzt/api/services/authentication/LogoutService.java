package com.vzt.api.services.authentication;

import com.vzt.api.models.authentication.User;
import com.vzt.api.models.session.BrowserSession;
import com.vzt.api.models.session.SessionLogin;
import com.vzt.api.repositories.authentication.UserRepository;
import com.vzt.api.repositories.session.BrowserSessionRepository;
import com.vzt.api.repositories.session.SessionLoginRepository;
import com.vzt.api.responses.ApplicationResponse;
import com.vzt.api.responses.ResponseStatus;
import com.vzt.api.services.UserByRequestService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LogoutService {
    private final BrowserSessionRepository browserSessionRepository;
    private final SessionService sessionService;
    private final UserByRequestService userByRequestService;

    public ApplicationResponse<?> logout(HttpServletRequest request) {
        User user = userByRequestService.get(request);
        if (user == null) {
            return new ApplicationResponse<>(ResponseStatus.UNAUTHORIZED, "User not found!", LocalDateTime.now(), null, null);
        }

        String sessionId = sessionService.getSessionId(request);
        if (sessionId == null) {
            return new ApplicationResponse<>(ResponseStatus.UNAUTHORIZED, "Session is empty!", LocalDateTime.now(), null, null);
        }

        Optional<BrowserSession> browserSessionOptional = browserSessionRepository.findBySessionId(UUID.fromString(sessionId));
        if (browserSessionOptional.isEmpty()) {
            return new ApplicationResponse<>(ResponseStatus.UNAUTHORIZED, "Session not found!", LocalDateTime.now(), null, null);
        }

        int activeUserId = request.getIntHeader("Active-User");

        BrowserSession browserSession = browserSessionOptional.get();
        SessionLogin sessionLogin = browserSession.getLogins().get(activeUserId);
        if (sessionLogin == null) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Session not found!", LocalDateTime.now(), null, null);
        }

        if (!sessionLogin.getAccessToken().equals(request.getHeader(HttpHeaders.AUTHORIZATION).substring(7))) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Token mismatch!", LocalDateTime.now(), null, null);
        }

        browserSession.getLogins().remove(activeUserId);
        browserSessionRepository.save(browserSession);
        return new ApplicationResponse<>(ResponseStatus.SUCCESS, "You are logged out!", LocalDateTime.now(), null, null);
    }
}