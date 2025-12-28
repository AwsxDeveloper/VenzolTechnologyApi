package com.vzt.api.services.realm;

import com.vzt.api.dtos.realm.OAuthCodeCreateDTO;
import com.vzt.api.models.authentication.User;
import com.vzt.api.models.realm.OAuthCode;
import com.vzt.api.models.realm.Realm;
import com.vzt.api.models.session.BrowserSession;
import com.vzt.api.repositories.realm.OAuthCodeRepository;
import com.vzt.api.repositories.realm.RealmRepository;
import com.vzt.api.responses.ApplicationResponse;
import com.vzt.api.responses.ResponseStatus;
import com.vzt.api.services.authentication.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class OAuthRedirectService {

    private final OAuthCodeRepository oAuthCodeRepository;
    private final SessionService sessionService;
    private final RealmRepository realmRepository;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    private String codeGenerator() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }


    private boolean isOnTheUserList(List<User> userList, User user) {
        for (User u : userList){
            if (u.getId().equals(user.getId())){
                return true;
            }
        }
        return false;
    }

    public ApplicationResponse<String> create(HttpServletRequest request, OAuthCodeCreateDTO dto) {
        String sessionId = sessionService.getSessionId(request);
        BrowserSession browserSession = sessionService.get(UUID.fromString(sessionId));
        int activeUserId = request.getIntHeader("Active-User");
        if (activeUserId < 0 || activeUserId > browserSession.getLogins().size()) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Invalid user!", LocalDateTime.now(), null, null);
        }

        Optional<Realm> realmOptional = realmRepository.findByRealmId(UUID.fromString(dto.getRealmId()));
        if (realmOptional.isEmpty()) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Realm not found!", LocalDateTime.now(), null, null);
        }

        Realm realm = realmOptional.get();

        if(realm.isDisabled()){
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Realm is disabled!", LocalDateTime.now(), null, null);
        }

        User user = browserSession.getLogins().get(activeUserId).getUser();

        if (!realm.isPubliclyAvailable()){
            if(!isOnTheUserList(realm.getRealmAdmins(), user)&&!isOnTheUserList(realm.getRealmUsers(), user)){
                return new ApplicationResponse<>(ResponseStatus.ERROR, "You do not have access to this realm!", LocalDateTime.now(), null, null);
            }
        }

        OAuthCode oAuthCode = new OAuthCode(
                null,
                realm,
                user,
                codeGenerator(),
                LocalDateTime.now()
        );
        oAuthCodeRepository.save(oAuthCode);

        String redirect_uri = realm.getRedirectURI()+"?response_type=code&code="+oAuthCode.getCode();
        return new ApplicationResponse<>(
                ResponseStatus.SUCCESS,
                "OAuth code created!",
                LocalDateTime.now(),
                null,
                redirect_uri
        );
    }

}
