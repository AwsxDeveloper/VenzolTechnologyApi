package com.vzt.api.services.authentication;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.vzt.api.config.JwtService;
import com.vzt.api.dtos.authentication.MFAVerifyDTO;
import com.vzt.api.models.authentication.MFASetting;
import com.vzt.api.models.authentication.User;
import com.vzt.api.models.session.BrowserSession;
import com.vzt.api.models.session.SessionLogin;
import com.vzt.api.repositories.authentication.MFASettingRepository;
import com.vzt.api.repositories.authentication.UserRepository;
import com.vzt.api.repositories.session.BrowserSessionRepository;
import com.vzt.api.responses.ApplicationResponse;
import com.vzt.api.responses.ResponseStatus;
import com.vzt.api.responses.authentication.MFAResponse;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MFAService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final SessionService sessionService;
    private final MFASettingRepository mfaSettingRepository;
    private final BrowserSessionRepository browserSessionRepository;
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    public String generateSecret() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    public String buildOtpAuthUrl(String issuer, String accountName, String secret) {
        String encIssuer = URLEncoder.encode(issuer, StandardCharsets.UTF_8);
        String encAccount = URLEncoder.encode(accountName, StandardCharsets.UTF_8);
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&digits=6&period=30",
                encIssuer, encAccount, secret, encIssuer
        );
    }

    public String qrPngDataUri(String otpAuthUrl, int size) throws IOException, WriterException {
        BitMatrix matrix = new QRCodeWriter().encode(otpAuthUrl, BarcodeFormat.QR_CODE, size, size);
        BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
        return "data:image/png;base64," + base64;
    }

    public boolean verify(String secret, String code) {
        return gAuth.authorize(secret, Integer.parseInt(code));
    }


    private String deletionCodeGenerator() {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder(16);

        for (int i = 0; i < 16; i++) {
            stringBuilder.append(secureRandom.nextInt(10));
        }

        return stringBuilder.toString();
    }

    public ApplicationResponse<MFAResponse> setMfa(HttpServletRequest request) throws IOException, WriterException {
        String jwt = request.getHeader(HttpHeaders.AUTHORIZATION).substring(7);
        String username = jwtService.extractUsername(jwt);
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return new ApplicationResponse<>(ResponseStatus.UNAUTHORIZED, "User not found!", LocalDateTime.now(), null, null);
        }
        User user = userOptional.get();
        MFASetting setting = new MFASetting(
                null,
                generateSecret(),
                deletionCodeGenerator(),
                LocalDateTime.now()
        );
        user.setMfaSetting(setting);
        userRepository.save(user);

        return new ApplicationResponse<>(ResponseStatus.SUCCESS, "Multi-Factor authentication was successfully configured!", LocalDateTime.now(), null,
                new MFAResponse(
                        qrPngDataUri(
                                buildOtpAuthUrl(
                                        "VZT AP",
                                        user.getUsername(),
                                        setting.getSecret()
                                ),
                                250
                        ),
                        setting.getSecret(),
                        setting.getDeletionKey()
                )
        );
    }

    public ApplicationResponse<?> verifyMfa(MFAVerifyDTO dto, HttpServletRequest request) {
        String mfaCookie = sessionService.getMfaToken(request);
        if (mfaCookie == null) {
            return new ApplicationResponse<>(ResponseStatus.UNAUTHORIZED, "MFA token not found!", LocalDateTime.now(), null, null);
        }

        Optional<BrowserSession> browserSessionOptional = browserSessionRepository.findByLogins_MfaToken(mfaCookie);
        if (browserSessionOptional.isEmpty()) {
            return new ApplicationResponse<>(ResponseStatus.UNAUTHORIZED, "Invalid MFA token!", LocalDateTime.now(), null, null);
        }

        BrowserSession browserSession = browserSessionOptional.get();

        Optional<User> userOptional = userRepository.findByUsername(dto.getUsername());

        if (userOptional.isEmpty()) {
            return new ApplicationResponse<>(ResponseStatus.UNAUTHORIZED, "User not found!", LocalDateTime.now(), null, null);
        }

        User user = userOptional.get();

        if (!verify(user.getMfaSetting().getSecret(), dto.getOtp())) {
            return new ApplicationResponse<>(ResponseStatus.UNAUTHORIZED, "Wrong code!", LocalDateTime.now(), null, null);
        }

        if (!Objects.equals(browserSession.getIpAddress(), request.getRemoteAddr()) || !Objects.equals(browserSession.getUserAgent(), request.getHeader("User-Agent"))) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "We went trouble while checking your signature!", LocalDateTime.now(), null, null);
        }

        String sessionId = sessionService.getSessionId(request);

        for (int i = 0; i < browserSession.getLogins().size(); i++) {
            if (browserSession.getLogins().get(i).getMfaToken().equals(mfaCookie)) {
                browserSession.getLogins().get(i).setMfaToken(null);
                break;
            }
        }

        browserSessionRepository.save(browserSession);

        if (sessionId == null) {
            return new ApplicationResponse<>(ResponseStatus.SUCCESS, "Successful verification!", LocalDateTime.now(), null, sessionService.createSession(user, dto.isTrusted(), request));
        }
        return new ApplicationResponse<>(ResponseStatus.SUCCESS, "Successful verification!", LocalDateTime.now(), null, sessionService.addLoginToExistingSession(user, dto.isTrusted(), request));
    }


    public ApplicationResponse<?> deleteMFAByCode(MFAVerifyDTO dto) {
        Optional<User> userOptional = userRepository.findByUsername(dto.getUsername());
        if (userOptional.isEmpty()) {
            return new ApplicationResponse<>(ResponseStatus.UNAUTHORIZED, "User not found!", LocalDateTime.now(), null, null);
        }

        User user = userOptional.get();

        if (!user.getMfaSetting().getDeletionKey().equals(dto.getOtp())) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Wrong code!", LocalDateTime.now(), null, null);
        }
        MFASetting setting = user.getMfaSetting();
        user.setMfaSetting(null);
        userRepository.save(user);
        mfaSettingRepository.delete(setting);

        return new ApplicationResponse<>(ResponseStatus.SUCCESS, "Multi-Factor authentication is now off!", LocalDateTime.now(), null, null);
    }
}
