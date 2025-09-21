package com.vzt.api.services.authentication;

import com.vzt.api.config.JwtService;
import com.vzt.api.models.authentication.User;
import com.vzt.api.models.session.BrowserSession;
import com.vzt.api.models.session.SessionLogin;
import com.vzt.api.repositories.session.BrowserSessionRepository;
import com.vzt.api.repositories.session.SessionLoginRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final BrowserSessionRepository browserSessionRepository;
    private final JwtService jwtService;
    private final SessionLoginRepository sessionLoginRepository;

    public String getSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String sessionId = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("SESSION_ID")) {
                    sessionId = cookie.getValue();
                }
            }
        }
        return sessionId;
    }



    public String getMfaToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String mfaToken = "";
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("MFA_TOKEN")) {
                    mfaToken = cookie.getValue();
                }
            }
        }
        return mfaToken;
    }


    public SessionLogin createLogin(User user){
        UUID uuid = UUID.randomUUID();
        while (sessionLoginRepository.existsByLoginId(uuid)) {
            uuid = UUID.randomUUID();
        }

        String mfaToken = null;
        String accessToken = null;
        String credentialExpiredToken = null;
        if (user.getMfaSetting()!=null){
            do {
                mfaToken = UUID.randomUUID().toString();
            }while (sessionLoginRepository.existsByMfaToken(mfaToken));
        }else {
            accessToken = jwtService.generateAccessToken(user);
        }

        SessionLogin sessionLogin = new SessionLogin(
                null,
                accessToken,
                uuid,
                mfaToken,
                user,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        return sessionLoginRepository.save(sessionLogin);
    }

    public BrowserSession createSession(User user, boolean trustedUser, HttpServletRequest request) {
        UUID uuid = UUID.randomUUID();
        while (browserSessionRepository.existsBySessionId(uuid)) {
            uuid = UUID.randomUUID();
        }
        BrowserSession browserSession = new BrowserSession(
                null,
                uuid,
                List.of(createLogin(user)),
                trustedUser?List.of(user.getUid()):null,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        return browserSessionRepository.save(browserSession);
    }

    public SessionLogin addLoginToExistingSession(User user, boolean trusted, HttpServletRequest request) {
        Optional<BrowserSession> optionalBrowserSession = browserSessionRepository.findBySessionId(UUID.fromString(getSessionId(request)));
        if (optionalBrowserSession.isEmpty()){
            return null;
        }
        BrowserSession browserSession = optionalBrowserSession.get();
        if (trusted){
            browserSession.getTrustedUsers().add(user.getUid());

        }
        SessionLogin sessionLogin = createLogin(user);
        browserSession.getLogins().add(sessionLogin);
        browserSessionRepository.save(browserSession);
        return sessionLogin;
    }

    public ResponseCookie createSessionIdCookie(String sessionId){
        return ResponseCookie
                .from("SESSION_ID", sessionId)
                .httpOnly(true)
                .path("/")
                .secure(false)
                .maxAge(Duration.ofDays(30))
                .build();
    }

    public ResponseCookie createMFACookie(String mfaToken){
        return ResponseCookie
                .from("MFA_TOKEN", mfaToken)
                .httpOnly(true)
                .path("/")
                .secure(false)
                .maxAge(Duration.ofMinutes(10))
                .build();
    }

    public boolean isCredentialExpired(SessionLogin sessionLogin){
        return sessionLogin.getUser().getCredentialExpire() != null && sessionLogin.getUser().getCredentialExpire().isAfter(LocalDateTime.now());
    }

    public int getActiveUserId(SessionLogin sessionLogin){
        Optional<BrowserSession> browserSessionOptional = browserSessionRepository.findByLoginsContains(sessionLogin);
        return browserSessionOptional.map(browserSession -> browserSession.getLogins().indexOf(sessionLogin)).orElse(-1);
    }

    public String updateAccessToken(UUID sessionId, User user){
        String accessToken = jwtService.generateAccessToken(user);
        Optional<BrowserSession> optionalBrowserSession = browserSessionRepository.findBySessionId(sessionId);
        if (optionalBrowserSession.isEmpty()){
            return null;
        }
        BrowserSession browserSession = optionalBrowserSession.get();
        for (int i = 0; i < browserSession.getLogins().size(); i++) {
            if (browserSession.getLogins().get(i).getUser().getUid()==user.getUid()){
                browserSession.getLogins().get(i).setAccessToken(accessToken);
                browserSessionRepository.save(browserSession);
                return accessToken;
            }
        }
        return null;
    }

}
