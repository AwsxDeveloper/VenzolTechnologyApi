package com.vzt.api.services.authentication;

import com.vzt.api.dtos.authentication.AuthenticationCheckDTO;
import com.vzt.api.dtos.authentication.EmailConfirmationDTO;
import com.vzt.api.dtos.authentication.RegisterDTO;
import com.vzt.api.models.authentication.*;
import com.vzt.api.models.security.Role;
import com.vzt.api.models.session.BrowserSession;
import com.vzt.api.models.session.SessionLogin;
import com.vzt.api.repositories.authentication.*;
import com.vzt.api.responses.ApplicationResponse;
import com.vzt.api.responses.ResponseStatus;
import com.vzt.api.services.mail.MailSenderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GenderRepository genderRepository;
    private final LanguageRepository languageRepository;
    private final CountryRepository countryRepository;
    private final UserDetailRepository userDetailRepository;
    private final RoleRepository roleRepository;
    private final MailSenderService mailSenderService;
    private final MailCodeRepository mailCodeRepository;
    private final SessionService sessionService;

    @Value("${mail-account.base_url}")
    private String baseUrl;

    public ApplicationResponse<?> checkUsername(AuthenticationCheckDTO registerCheckDTO) {
        if (userRepository.existsByUsername(registerCheckDTO.getContent())) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "This username is already taken!", LocalDateTime.now(),null, null);
        }
        return new ApplicationResponse<>(ResponseStatus.SUCCESS, "This username is unused!", LocalDateTime.now(),null, null);
    }

    public ApplicationResponse<?> checkPhoneNumber(AuthenticationCheckDTO registerCheckDTO) {
        if (userDetailRepository.existsByPhoneNumber(registerCheckDTO.getContent())) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "This phone number is already taken!", LocalDateTime.now(),null, null);
        }
        return new ApplicationResponse<>(ResponseStatus.SUCCESS, "This phone number is unused!", LocalDateTime.now(),null, null);
    }

    public ApplicationResponse<?> checkEmail(AuthenticationCheckDTO registerCheckDTO) {
        if (userRepository.existsByEmail(registerCheckDTO.getContent())) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "This email is already taken!", LocalDateTime.now(),null, null);
        }
        return new ApplicationResponse<>(ResponseStatus.SUCCESS, "This email is unused!", LocalDateTime.now(),null, null);
    }

    private Role getRoleByName(String name) {
        Optional<Role> role = roleRepository.findByName(name);
        if (role.isEmpty()) {
            throw new RuntimeException("Role not found!");
        }
        return role.get();
    }

    private UUID generateUUID() {
        UUID uuid = UUID.randomUUID();
        while (userRepository.existsByUid(uuid)){
            uuid = UUID.randomUUID();
        }
        return uuid;
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

    private MailCode sendRegisterConfirmationEmail(String email, String fullName) {
        String confId = UUID.randomUUID().toString();
        while(mailCodeRepository.existsByCode(confId)){
            confId = UUID.randomUUID().toString();
        }

        MailCode mailCode = new MailCode(null, confId, LocalDateTime.now());

        String link = baseUrl+"/register/confirm-email?id="+mailCode.getCode();
        Context context = new Context();
        context.setVariable("name", fullName);
        context.setVariable("year", String.valueOf(new Date().getYear() + 1900));
        context.setVariable("link",link);
        context.setVariable("confirmation_id", mailCode.getCode());
        if (mailSenderService.send(email, "Registration confirmation", "email_confirmation", context)==MailStatus.FAILED){
            return null;
        }
        return mailCode;
    }

    public ApplicationResponse<?> register(RegisterDTO registerDTO) {
        if (userRepository.existsByEmail(registerDTO.getEmail())){
            return new ApplicationResponse<>(ResponseStatus.ERROR, "This email is already taken!", LocalDateTime.now(),null, null);
        }

        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "This username is already taken!", LocalDateTime.now(),null, null);
        }

        if (userDetailRepository.existsByPhoneNumber(registerDTO.getPhoneNumber())) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "This phone number is already taken!", LocalDateTime.now(),null, null);
        }

        UserDetail  userDetail = new UserDetail(
                null,
                registerDTO.getFirstName(),
                registerDTO.getLastName(),
                null,
                registerDTO.getPhoneNumber(),
                registerDTO.getBirthDate(),
                getLanguage(registerDTO.getLanguage()),
                getCountry(registerDTO.getCountry()),
                getGender(registerDTO.getGender())
        );

        User user = new User(
                null,
                generateUUID(),
                registerDTO.getUsername(),
                registerDTO.getEmail(),
                passwordEncoder.encode(registerDTO.getPassword()),
                Set.of(getRoleByName("USER")),
                true,
                false,
                true,
                null,
                null,
                userDetail,
                null,
                null,
                sendRegisterConfirmationEmail(registerDTO.getEmail(), userDetail.getFirstName()+" "+userDetail.getLastName()),
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        userRepository.save(user);
        return new ApplicationResponse<>(ResponseStatus.SUCCESS, "User created successfully!", LocalDateTime.now(),null, null);
    }

    public ApplicationResponse<?> confirmRegistration(HttpServletRequest request, EmailConfirmationDTO dto){
        Optional<User> userOptional = userRepository.findByMailCode_Code(dto.getCode());
        if (userOptional.isEmpty()){
            return new ApplicationResponse<>(ResponseStatus.ERROR, "This code is not exist!", LocalDateTime.now(),null, null);
        }

        User user = userOptional.get();
        if (LocalDateTime.now().isAfter(user.getMailCode().getCreatedAt().plusHours(1))){
            return new ApplicationResponse<>(ResponseStatus.ERROR, "This code is expired!", LocalDateTime.now(),null, null);
        }
        MailCode mailCode = user.getMailCode();
        user.setMailCode(null);
        user.setAccountVerified(true);
        userRepository.save(user);
        mailCodeRepository.delete(mailCode);
        if (sessionService.getSessionId(request)==null){
            BrowserSession browserSession = sessionService.createSession(user, false, request, false);
            SessionLogin sessionLogin = browserSession.getLogins().get(0);
            return new ApplicationResponse<BrowserSession>(ResponseStatus.SUCCESS, "You successfully confirmed your email!", LocalDateTime.now(), sessionLogin.getAccessToken(), browserSession);
        }

        SessionLogin sessionLogin = sessionService.addLoginToExistingSession(user, false,request, false);
        return new ApplicationResponse<SessionLogin>(ResponseStatus.SUCCESS, "You successfully confirmed your email!", LocalDateTime.now(), sessionLogin.getAccessToken(), sessionLogin);
    }

}
