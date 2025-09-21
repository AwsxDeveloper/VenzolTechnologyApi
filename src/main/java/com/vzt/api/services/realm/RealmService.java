package com.vzt.api.services.realm;

import com.vzt.api.dtos.realm.CreateRealmDTO;
import com.vzt.api.models.BillingStatus;
import com.vzt.api.models.authentication.User;
import com.vzt.api.models.realm.Realm;
import com.vzt.api.models.realm.RealmLogo;
import com.vzt.api.models.realm.RealmPlan;
import com.vzt.api.models.realm.RealmSubscription;
import com.vzt.api.repositories.realm.RealmLogoRepository;
import com.vzt.api.repositories.realm.RealmRepository;
import com.vzt.api.repositories.realm.RealmSubscriptionRepository;
import com.vzt.api.responses.ApplicationResponse;
import com.vzt.api.responses.realm.RealmAdminResponse;
import com.vzt.api.responses.realm.RealmFullAndUsernameEmailResponse;
import com.vzt.api.responses.realm.RealmResponse;
import com.vzt.api.responses.ResponseStatus;
import com.vzt.api.responses.realm.RealmSubscriptionResponse;
import com.vzt.api.services.UserByRequestService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RealmService {

    @Value("${realm.base_url}")
    private String realmImageUrl;

    private final RealmRepository realmRepository;
    private final RealmLogoRepository realmLogoRepository;
    private final UserByRequestService userByRequestService;
    private final RealmSubscriptionRepository realmSubscriptionRepository;


    private String generateSecret() {
        String randomKey;
        boolean exists = false;
        do {
            SecureRandom secureRandom = new SecureRandom();
            byte[] randomBytes = new byte[64];
            secureRandom.nextBytes(randomBytes);
            randomKey = new BigInteger(1, randomBytes).toString(16).toUpperCase();
            exists = realmRepository.existsBySecret(randomKey);
        } while (exists);
        return randomKey;
    }

    private UUID generateRealmId() {
        UUID uuid = UUID.randomUUID();
        while (realmRepository.existsByRealmId(uuid)) {
            uuid = UUID.randomUUID();
        }
        return uuid;
    }
    private UUID generateRealmSubscriptionId() {
        UUID uuid = UUID.randomUUID();
        while (realmSubscriptionRepository.existsBySubscriptionId(uuid)) {
            uuid = UUID.randomUUID();
        }
        return uuid;
    }

    private UUID generateRealmLogoId() {
        UUID uuid = UUID.randomUUID();
        while (realmLogoRepository.existsByImageId(uuid)) {
            uuid = UUID.randomUUID();
        }
        return uuid;
    }

    public RealmLogo uploadRealmLogoOnCreate(MultipartFile file) throws IOException {
        RealmLogo logo = new RealmLogo(
                null,
                generateRealmLogoId(),
                file.getContentType(),
                file.getBytes(),
                LocalDateTime.now()
        );
        return realmLogoRepository.save(logo);
    }

    public ApplicationResponse<List<RealmAdminResponse>> getOwnedRealms(HttpServletRequest request) {
        User user = userByRequestService.get(request);
        if (user == null) {
            return new ApplicationResponse<>(ResponseStatus.UNAUTHORIZED, "User not found!", LocalDateTime.now(), null, null);
        }

        List<Realm> realms = realmRepository.findAllByRealmAdminsContains(user);

        if (realms.isEmpty()) {
            return new ApplicationResponse<>(ResponseStatus.SUCCESS, "You does not have any realm!", LocalDateTime.now(), null, null);
        }

        List<RealmAdminResponse> realmAdminResponseList = new ArrayList<>();
        for (Realm realm : realms) {
            RealmFullAndUsernameEmailResponse createdBy = new RealmFullAndUsernameEmailResponse(
                    realm.getCreatedBy().getUsername(),
                    realm.getCreatedBy().getUserDetail().getFirstName() + " " + realm.getCreatedBy().getUserDetail().getLastName(),
                    realm.getCreatedBy().getEmail()
            );

            List<RealmFullAndUsernameEmailResponse> adminList = new ArrayList<>();

            for (User admin :
                    realm.getRealmAdmins()) {
                adminList.add(new RealmFullAndUsernameEmailResponse(
                        admin.getUsername(),
                        admin.getUserDetail().getFirstName() + " " + admin.getUserDetail().getLastName(),
                        admin.getEmail()
                ));
            }

            List<RealmFullAndUsernameEmailResponse> userList = new ArrayList<>();

            for (User realmUser :
                    realm.getRealmUsers()) {
                userList.add(new RealmFullAndUsernameEmailResponse(
                        realmUser.getUsername(),
                        realmUser.getUserDetail().getFirstName() + " " + realmUser.getUserDetail().getLastName(),
                        realmUser.getEmail()
                ));
            }

            RealmSubscriptionResponse subscriptionResponse = new RealmSubscriptionResponse(
              realm.getSubscription().getSubscriptionId(),
              realm.getSubscription().getSubscriptionPlan(),
              realm.getSubscription().getBillingStatus(),
              realm.getSubscription().getCreatedAt(),
              realm.getSubscription().getRenewedAt(),
              realm.getSubscription().getEndAt()
            );

            realmAdminResponseList.add(
                    new RealmAdminResponse(
                            realm.getRealmId().toString(),
                            realm.getDisplayName(),
                            realm.getSecret(),
                            realm.getLogo(),
                            createdBy,
                            adminList,
                            userList,
                            realm.getOrigin(),
                            realm.getRedirectURI(),
                            realm.getHelperEmail(),
                            subscriptionResponse,
                            realm.isPubliclyAvailable(),
                            realm.isDisabled(),
                            realm.getCreatedAt(),
                            realm.getLastModifiedDate()
                    )
            );

        }

        return new ApplicationResponse<>(ResponseStatus.SUCCESS, "Owned realms loaded!", LocalDateTime.now(), null, realmAdminResponseList);

    }

    public Optional<RealmLogo> getRealmLogo(String imageId) {
        return realmLogoRepository.findByImageId(UUID.fromString(imageId));
    }

    public ApplicationResponse<RealmResponse> getRealmById(UUID realmId) {
        Optional<Realm> realmOptional = realmRepository.findByRealmId(realmId);
        if (realmOptional.isEmpty()) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Realm not found!", LocalDateTime.now(), null, null);
        }
        Realm realm = realmOptional.get();
        RealmResponse realmResponse = new RealmResponse(
                realm.getLogo(),
                realm.getDisplayName(),
                realm.getHelperEmail(),
                realm.getRedirectURI()
        );
        return new ApplicationResponse<>(ResponseStatus.SUCCESS, "Realm loaded", LocalDateTime.now(), null, realmResponse);
    }

    public ApplicationResponse<List<RealmAdminResponse>> create(HttpServletRequest request, CreateRealmDTO dto, MultipartFile file) throws IOException {
        User user = userByRequestService.get(request);
        if (user == null) {
            return new ApplicationResponse<>(ResponseStatus.UNAUTHORIZED, "User not found!", LocalDateTime.now(), null, null);
        }

        RealmSubscription subscription = new RealmSubscription(
                null,
                generateRealmSubscriptionId(),
                RealmPlan.FREE,
                BillingStatus.PAID,
                LocalDateTime.now(),
                null,
                null
        );

        Realm realm = new Realm(
                null,
                generateRealmId(),
                dto.getName(),
                generateSecret(),
                realmImageUrl + uploadRealmLogoOnCreate(file).getImageId(),
                Set.of(user),
                user,
                dto.getOrigin(),
                dto.getRedirectURL(),
                subscription,
                false,
                dto.getPlan() != RealmPlan.FREE && dto.isPubliclyAvailable(),
                null,
                dto.getHelperEmail(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        realmRepository.save(realm);

        ApplicationResponse<List<RealmAdminResponse>> realmAdminResponseList = getOwnedRealms(request);
        if (realmAdminResponseList.getStatus() == ResponseStatus.SUCCESS) {
            return new ApplicationResponse<>(ResponseStatus.SUCCESS, "Realm created!", LocalDateTime.now(), null, realmAdminResponseList.getData());
        }

        return new ApplicationResponse<>(ResponseStatus.ERROR, "Realm creation failed!", LocalDateTime.now(), null, null);
    }

}
