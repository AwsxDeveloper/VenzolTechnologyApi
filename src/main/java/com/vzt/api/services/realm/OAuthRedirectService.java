package com.vzt.api.services.realm;

import com.vzt.api.dtos.realm.CallBackDTO;
import com.vzt.api.dtos.realm.OAuthCodeCreateDTO;
import com.vzt.api.models.authentication.User;
import com.vzt.api.models.realm.OAuthCode;
import com.vzt.api.models.realm.Realm;
import com.vzt.api.models.session.BrowserSession;
import com.vzt.api.repositories.realm.OAuthCodeRepository;
import com.vzt.api.repositories.realm.RealmRepository;
import com.vzt.api.responses.ApplicationResponse;
import com.vzt.api.responses.ResponseStatus;
import com.vzt.api.responses.realm.CallBackResponse;
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
        for (User u : userList) {
            if (u.getId().equals(user.getId())) {
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

        if (realm.isDisabled()) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Realm is disabled!", LocalDateTime.now(), null, null);
        }

        User user = browserSession.getLogins().get(activeUserId).getUser();

        if (!realm.isPubliclyAvailable()) {
            if (!isOnTheUserList(realm.getRealmAdmins(), user) && !isOnTheUserList(realm.getRealmUsers(), user)) {
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

        String redirect_uri = realm.getRedirectURI() + "?response_type=code&code=" + oAuthCode.getCode();
        return new ApplicationResponse<>(
                ResponseStatus.SUCCESS,
                "OAuth code created!",
                LocalDateTime.now(),
                null,
                redirect_uri
        );
    }

    public ApplicationResponse<CallBackResponse> callBack(HttpServletRequest request, CallBackDTO dto) {
        Optional<OAuthCode> oAuthCodeOptional = oAuthCodeRepository.findByCode(dto.getCode());
        if (oAuthCodeOptional.isEmpty()) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Invalid code!", LocalDateTime.now(), null, null);
        }

        OAuthCode oAuthCode = oAuthCodeOptional.get();

        LocalDateTime now = LocalDateTime.now().minusMinutes(5);
        if (oAuthCode.getCreatedAt().isBefore(now)){
            return new ApplicationResponse<>(ResponseStatus.ERROR, "The code is expired!", LocalDateTime.now(), null, null);
        }

        String requestOrigin = request.getHeader("Origin");

        if (!requestOrigin.equals(oAuthCode.getRealm().getOrigin())) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Invalid origin!", LocalDateTime.now(), null, null);
        }

        if (oAuthCode.getRealm().isDisabled()) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Realm is disabled!", LocalDateTime.now(), null, null);
        }

        if (!oAuthCode.getRealm().isPubliclyAvailable()) {
            if (!(isOnTheUserList(oAuthCode.getRealm().getRealmUsers(), oAuthCode.getUser()) || isOnTheUserList(oAuthCode.getRealm().getRealmAdmins(), oAuthCode.getUser()))) {
                return new ApplicationResponse<>(ResponseStatus.UNAUTHORIZED, "You do not have enough permission to this realm!", LocalDateTime.now(), null, null);
            }
        }

        if (!oAuthCode.getRealm().getRealmId().equals(UUID.fromString(dto.getRealmId()))) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Realm ID does not match!", LocalDateTime.now(), null, null);
        }

        if (!oAuthCode.getRealm().getSecret().equals(dto.getSecret())) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Secret does not match!", LocalDateTime.now(), null, null);
        }

        User user = oAuthCode.getUser();
        CallBackResponse response = CallBackResponse.builder()
                .firstName(user.getUserDetail().getFirstName())
                .lastName(user.getUserDetail().getLastName())
                .email(user.getEmail())
                .username(user.getUsername())
                .userId(user.getUid().toString())
                .country(user.getUserDetail().getCountry().getCountryName())
                .gender(user.getUserDetail().getGender().getName())
                .language(user.getUserDetail().getLanguage().getLongName())
                .dateOfBirth(user.getUserDetail().getBirthday())
                .profilePicture(user.getUserDetail().getProfilePicture())
                .build();

        oAuthCodeRepository.delete(oAuthCode);
        return new ApplicationResponse<>(ResponseStatus.SUCCESS, "Callback successfully completed!", LocalDateTime.now(), null, response);

    }

}
