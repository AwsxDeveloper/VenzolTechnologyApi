package com.vzt.api.services.authentication;

import com.vzt.api.config.CustomAuthProvider;
import com.vzt.api.config.JwtService;
import com.vzt.api.dtos.authentication.AuthenticationCheckDTO;
import com.vzt.api.dtos.authentication.UsernamePasswordDTO;
import com.vzt.api.models.authentication.User;
import com.vzt.api.models.realm.Realm;
import com.vzt.api.models.session.BrowserSession;
import com.vzt.api.models.session.SessionLogin;
import com.vzt.api.repositories.authentication.UserRepository;
import com.vzt.api.repositories.realm.RealmRepository;
import com.vzt.api.repositories.session.BrowserSessionRepository;
import com.vzt.api.responses.ApplicationResponse;
import com.vzt.api.responses.ResponseStatus;
import com.vzt.api.responses.authentication.UserDetailsForLoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final UserRepository userRepository;
    private final RealmRepository realmRepository;
    private final CustomAuthProvider  customAuthProvider;
    private final SessionService sessionService;
    private final BrowserSessionRepository browserSessionRepository;

    private boolean isEmail(String username) {
        return Pattern.compile("^(.+)@(\\S+)$").matcher(username).matches();
    }

    private int isLoggedIn(HttpServletRequest request, User user) {
        String sessionId = sessionService.getSessionId(request);
        if(sessionId == null) {
            return -1;
        }

        Optional<BrowserSession> browserSessionOptional = browserSessionRepository.findBySessionId(UUID.fromString(sessionId));
        if (browserSessionOptional.isEmpty()){
            return -1;
        }

        BrowserSession browserSession = browserSessionOptional.get();

        int activeUserId = -1;
        for (int i = 0; i < browserSession.getLogins().size(); i++) {
            if (browserSession.getLogins().get(i).getUser()==user) {
                activeUserId = i;
                break;
            }
        }

        return activeUserId;
    }

    public ApplicationResponse<?> checkUsernameOrEmail(HttpServletRequest request, AuthenticationCheckDTO dto) {
        Optional<User> userOptional;
        if (isEmail(dto.getContent().toLowerCase())) {
            userOptional= userRepository.findByEmail(dto.getContent());
            if (userOptional.isEmpty()) {
                return new ApplicationResponse<>(ResponseStatus.ERROR, "Invalid email!", LocalDateTime.now(), null, null);
            }
        }

        userOptional = userRepository.findByUsername(dto.getContent());
        if (userOptional.isEmpty()){
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Invalid username!", LocalDateTime.now(), null, null);
        }



        User user = userOptional.get();
        UserDetailsForLoginResponse userDetailsForLoginResponse = new UserDetailsForLoginResponse(
                user.getUserDetail().getProfilePicture(),
                user.getUserDetail().getFirstName()+" "+user.getUserDetail().getLastName(),
                user.getUsername(),
                isLoggedIn(request, user)
        );
        return new ApplicationResponse<UserDetailsForLoginResponse>(ResponseStatus.SUCCESS, "User details loaded!", LocalDateTime.now(), null, userDetailsForLoginResponse);
    }

    private boolean hasRealmAccess(User user, UUID realmId) {
        Optional<Realm> realmOptional = realmRepository.findByRealmId(realmId);
        if (realmOptional.isEmpty()) {
            return false;
        }

        Realm realm = realmOptional.get();
        if (realm.isDisabled()) {
            return false;
        }

        if (realm.isPubliclyAvailable()) {
            return true;
        }

        return realm.getRealmUsers().contains(user) || realm.getRealmAdmins().contains(user);

    }

    public ApplicationResponse<?> login(HttpServletRequest request, UsernamePasswordDTO dto){
        Optional<User> userOptional;
        if (isEmail(dto.getUsername()))
            userOptional = userRepository.findByEmail(dto.getUsername());
        else
            userOptional = userRepository.findByUsername(dto.getUsername());

        if (userOptional.isEmpty()){
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Invalid "+(isEmail(dto.getUsername())?"email":"username")+"!", LocalDateTime.now(), null, null);
        }

        User user = userOptional.get();


        try {
            Authentication authentication = customAuthProvider.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            dto.getUsername(),
                            dto.getPassword()
                    )
            );

            if (!user.isAccountNonLocked()){
                return new ApplicationResponse<>(ResponseStatus.ERROR, "Account locked!", LocalDateTime.now(), null, null);
            }

            if (!user.isAccountVerified()){
                return new ApplicationResponse<>(ResponseStatus.ERROR, "Account not verified!", LocalDateTime.now(), null, null);
                //TODO: send verification email
            }

            if(dto.getRealmId()!=null){

                if (!hasRealmAccess(user, UUID.fromString(dto.getRealmId()))){
                    return new ApplicationResponse<>(ResponseStatus.ERROR, "You do not have access to this realm, please contact the administrator!", LocalDateTime.now(), null, null);
                }
            }

            String sessionId = sessionService.getSessionId(request);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            if (sessionId == null) {
               return new ApplicationResponse<>(ResponseStatus.SUCCESS,"Session created!",  LocalDateTime.now(), null, sessionService.createSession(user, false, request));
            }

            SessionLogin sessionLogin = sessionService.addLoginToExistingSession(user, false,request);
            return new ApplicationResponse<>(ResponseStatus.SUCCESS, "User added to the session!", LocalDateTime.now(), null,  sessionLogin);

        } catch (BadCredentialsException e) {
            return new ApplicationResponse<>(ResponseStatus.UNAUTHORIZED, "Wrong password!", LocalDateTime.now(), null, null);
        }}

}
