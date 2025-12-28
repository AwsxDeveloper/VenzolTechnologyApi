package com.vzt.api.services.account;

import com.vzt.api.config.CustomAuthProvider;
import com.vzt.api.config.JwtService;
import com.vzt.api.dtos.account.ChangePasswordDTO;
import com.vzt.api.dtos.account.UpdateProfileDTO;
import com.vzt.api.models.authentication.*;
import com.vzt.api.models.session.BrowserSession;
import com.vzt.api.repositories.authentication.*;
import com.vzt.api.repositories.session.BrowserSessionRepository;
import com.vzt.api.responses.ApplicationResponse;
import com.vzt.api.responses.ResponseStatus;
import com.vzt.api.responses.account.LoggedInUsersResponse;
import com.vzt.api.responses.account.ProfileResponse;
import com.vzt.api.services.authentication.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AccountService {

    @Value("${image.base_url}")
    private String imageBaseUrl;

    private final BrowserSessionRepository  browserSessionRepository;
    private final SessionService sessionService;
    private final GenderRepository genderRepository;
    private final LanguageRepository languageRepository;
    private final CountryRepository countryRepository;
    private final ProfilePictureRepository profilePictureRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthProvider authenticationProvider;

    public ApplicationResponse<List<LoggedInUsersResponse>> getLoggedInUsers(HttpServletRequest request) {
        String sessionId = sessionService.getSessionId(request);

        if(sessionId == null){
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Session empty!", LocalDateTime.now(), null, null);
        }

        Optional<BrowserSession> browserSessionOptional = browserSessionRepository.findBySessionId(UUID.fromString(sessionId));
        if(browserSessionOptional.isEmpty()){
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Session not found!", LocalDateTime.now(), null, null);
        }

        BrowserSession browserSession = browserSessionOptional.get();

        List<LoggedInUsersResponse> loggedInUsers = new ArrayList<>();

        for (int i = 0; i < browserSession.getLogins().size(); i++) {
            User user = browserSession.getLogins().get(i).getUser();
            loggedInUsers.add(
                    new LoggedInUsersResponse(
                            i,
                            user.getUserDetail().getFirstName()+" "+user.getUserDetail().getLastName(),
                            user.getUserDetail().getProfilePicture(),
                            browserSession.getLogins().get(i).getAccessToken()
                    )
            );
        }

        return new ApplicationResponse<>(ResponseStatus.SUCCESS, "Logged in users loaded!", LocalDateTime.now(), null, loggedInUsers);
    }


    private Language getLanguage(String shortName) {
        return languageRepository.findByShortName(shortName).orElseThrow(() -> new RuntimeException("Language not found"));
    }

    private Gender getGender(String name) {
        return genderRepository.findByName(name).orElseThrow(() -> new RuntimeException("Gender not found"));
    }

    private Country getCountry(String code) {
        return countryRepository.findByCountryCode(code).orElseThrow(() -> new RuntimeException("Country not found"));
    }

    private UUID generateUUID() {
        UUID uuid = UUID.randomUUID();
        while (profilePictureRepository.existsByImageId(uuid)) {
            uuid = UUID.randomUUID();
        }
        return uuid;
    }

    private String getUsernameByRequest(HttpServletRequest request) {
        String jwt = request.getHeader(HttpHeaders.AUTHORIZATION).substring(7);
        String username = jwtService.extractUsername(jwt);
        Optional<User> userOptional = userRepository.findByUsername(username);
        return userOptional.map(User::getUsername).orElse(null);
    }


    public ApplicationResponse<ProfileResponse> getProfileData(User user) {
        ProfileResponse profileResponse = new ProfileResponse(
                user.getUserDetail().getFirstName(),
                user.getUserDetail().getLastName(),
                user.getEmail(),
                user.getUsername(),
                user.getUserDetail().getPhoneNumber(),
                user.getUserDetail().getBirthday(),
                user.getUserDetail().getGender().getName(),
                user.getUserDetail().getCountry().getCountryCode(),
                user.getUserDetail().getLanguage().getShortName(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );

        return new ApplicationResponse<>(ResponseStatus.SUCCESS, "User details loaded!", LocalDateTime.now(),null, profileResponse);
    }

    private boolean unUsedEmail(String newEmail, String oldEmail) {
        if (newEmail.equals(oldEmail)) {
            return true;
        }
        return userRepository.existsByEmail(newEmail);
    }

    public ApplicationResponse<ProfileResponse> updateProfileData(HttpServletRequest request, User user, UpdateProfileDTO updateProfileDTO) {
        if (!unUsedEmail(updateProfileDTO.getEmail(), user.getEmail())) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Email is already taken!", LocalDateTime.now(), null, null);
        }

        if (!user.getEmail().equals(updateProfileDTO.getEmail())) {
            user.setAccountVerified(false);
        }

        user.setEmail(updateProfileDTO.getEmail());
        user.getUserDetail().setFirstName(updateProfileDTO.getFirstName());
        user.getUserDetail().setLastName(updateProfileDTO.getLastName());
        user.getUserDetail().setPhoneNumber(updateProfileDTO.getPhoneNumber());
        user.getUserDetail().setGender(getGender(updateProfileDTO.getGender()));
        user.getUserDetail().setCountry(getCountry(updateProfileDTO.getCountry()));
        user.getUserDetail().setLanguage(getLanguage(updateProfileDTO.getLanguage()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        ApplicationResponse<ProfileResponse> response = getProfileData(user);
        if (response.getStatus() != ResponseStatus.SUCCESS) {
            return response;
        }

        String sessionId = sessionService.getSessionId(request);
        if(sessionId == null){
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Session empty!", LocalDateTime.now(), null, null);
        }
        String accessToken = sessionService.updateAccessToken(UUID.fromString(sessionId), user);

        if (accessToken == null) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Access token not found!", LocalDateTime.now(), null, null);
        }

        return new ApplicationResponse<>(ResponseStatus.SUCCESS, "Your profile has been updated!", LocalDateTime.now(), accessToken, response.getData());
    }

    public ApplicationResponse<?> uploadProfilePicture(HttpServletRequest request, User user, MultipartFile file) throws IOException {

        ProfilePicture profilePicture = new ProfilePicture(
                null,
                generateUUID(),
                file.getContentType(),
                file.getBytes(),
                LocalDateTime.now()
        );
        profilePictureRepository.save(profilePicture);
        user.getUserDetail().setProfilePicture(imageBaseUrl + profilePicture.getImageId());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        String sessionId = sessionService.getSessionId(request);
        if(sessionId == null){
            return new ApplicationResponse<>(ResponseStatus.UNAUTHORIZED, "Session empty!", LocalDateTime.now(), null, null);
        }

        String  accessToken = sessionService.updateAccessToken(UUID.fromString(sessionId), user);
        if (accessToken == null) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Access token not found!", LocalDateTime.now(), null, null);
        }

        return new ApplicationResponse<>(ResponseStatus.SUCCESS, "Your profile picture was successfully uploaded!", LocalDateTime.now(), accessToken, null);
    }

    public ApplicationResponse<?> deleteProfilePicture(HttpServletRequest request, User user) {
        user.setUpdatedAt(LocalDateTime.now());
        user.getUserDetail().setProfilePicture(null);
        userRepository.save(user);

        String sessionId = sessionService.getSessionId(request);
        if(sessionId == null){
            return new ApplicationResponse<>(ResponseStatus.UNAUTHORIZED, "Session empty!", LocalDateTime.now(), null, null);
        }

        String  accessToken = sessionService.updateAccessToken(UUID.fromString(sessionId), user);
        if (accessToken == null) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Access token not found!", LocalDateTime.now(), null, null);
        }
        return new ApplicationResponse<>(ResponseStatus.SUCCESS, "Your profile picture was successfully deleted!", LocalDateTime.now(), accessToken, null);
    }

    public Optional<ProfilePicture> getProfilePicture(String imageId) throws IOException {
        return profilePictureRepository.findByImageId(UUID.fromString(imageId));
    }

    public ApplicationResponse<?> changePassword(User user, ChangePasswordDTO dto) {
        try {
            Authentication authentication = authenticationProvider.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), dto.getCurrentPassword())
            );
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            userRepository.save(user);
            return new ApplicationResponse<>(ResponseStatus.SUCCESS, "Your password has been changed!", LocalDateTime.now(), null, null);
        }catch (BadCredentialsException e) {
            return new ApplicationResponse<>(ResponseStatus.UNAUTHORIZED, "Bad Credentials", LocalDateTime.now(), null, null);
        }

    }



}
