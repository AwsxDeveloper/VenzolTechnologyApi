package com.vzt.api.services.realm;

import com.vzt.api.dtos.realm.*;
import com.vzt.api.models.BillingStatus;
import com.vzt.api.models.authentication.User;
import com.vzt.api.models.realm.Realm;
import com.vzt.api.models.realm.RealmLogo;
import com.vzt.api.models.realm.RealmPlan;
import com.vzt.api.models.realm.RealmSubscription;
import com.vzt.api.repositories.authentication.UserRepository;
import com.vzt.api.repositories.realm.RealmLogoRepository;
import com.vzt.api.repositories.realm.RealmRepository;
import com.vzt.api.repositories.realm.RealmSubscriptionRepository;
import com.vzt.api.responses.ApplicationResponse;
import com.vzt.api.responses.realm.RealmAdminResponse;
import com.vzt.api.responses.realm.RealmFullAndUsernameEmailResponse;
import com.vzt.api.responses.realm.RealmResponse;
import com.vzt.api.responses.ResponseStatus;
import com.vzt.api.responses.realm.RealmSubscriptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
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
    private final UserRepository userRepository;
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

    public ApplicationResponse<List<RealmAdminResponse>> getOwnedRealms(User user) {
        List<Realm> realms = realmRepository.findAllByRealmAdminsContains(user);

        if (realms.isEmpty()) {
            return new ApplicationResponse<>(ResponseStatus.SUCCESS, "You does not have any realm!", LocalDateTime.now(), null, null);
        }

        List<RealmAdminResponse> realmAdminResponseList = new ArrayList<>();
        for (Realm realm : realms) {
            RealmFullAndUsernameEmailResponse createdBy = new RealmFullAndUsernameEmailResponse(
                    realm.getCreatedBy().getUsername(),
                    realm.getCreatedBy().getUserDetail().getFirstName() + " " + realm.getCreatedBy().getUserDetail().getLastName(),
                    realm.getCreatedBy().getEmail(),
                    realm.getCreatedBy().getUid(),
                    realm.getCreatedBy().getUserDetail().getProfilePicture()
            );

            List<RealmFullAndUsernameEmailResponse> adminList = new ArrayList<>();

            for (User admin :
                    realm.getRealmAdmins()) {
                adminList.add(new RealmFullAndUsernameEmailResponse(
                        admin.getUsername(),
                        admin.getUserDetail().getFirstName() + " " + admin.getUserDetail().getLastName(),
                        admin.getEmail(),
                        admin.getUid(),
                        admin.getUserDetail().getProfilePicture()
                ));
            }

            List<RealmFullAndUsernameEmailResponse> userList = new ArrayList<>();
            if (realm.getRealmUsers() != null) {
                for (User realmUser :
                        realm.getRealmUsers()) {
                    userList.add(new RealmFullAndUsernameEmailResponse(
                            realmUser.getUsername(),
                            realmUser.getUserDetail().getFirstName() + " " + realmUser.getUserDetail().getLastName(),
                            realmUser.getEmail(),
                            realmUser.getUid(),
                            realmUser.getUserDetail().getProfilePicture()
                    ));
                }
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

    public ApplicationResponse<List<RealmAdminResponse>> create(User user, CreateRealmDTO dto, MultipartFile file) throws IOException {
        RealmSubscription subscription = new RealmSubscription(
                null,
                generateRealmSubscriptionId(),
                dto.getPlan(),
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
                List.of(user),
                user,
                dto.getOrigin(),
                dto.getRedirectURL(),
                subscription,
                false,
                dto.isPubliclyAvailable(),
                null,
                dto.getHelperEmail(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        realmRepository.save(realm);

        ApplicationResponse<List<RealmAdminResponse>> realmAdminResponseList = getOwnedRealms(user);
        if (realmAdminResponseList.getStatus() == ResponseStatus.SUCCESS) {
            return new ApplicationResponse<>(ResponseStatus.SUCCESS, "Realm created!", LocalDateTime.now(), null, realmAdminResponseList.getData());
        }

        return new ApplicationResponse<>(ResponseStatus.ERROR, "Realm creation failed!", LocalDateTime.now(), null, null);
    }

    private List<Long> excludedIds(List<User> users) {
        return users.stream().map(User::getId).toList();
    }

    public ApplicationResponse<List<RealmFullAndUsernameEmailResponse>> getHitForAdminAutoComplete(AutocompleteUserToMemberListDTO dto) {
        Optional<Realm> realmOptional = realmRepository.findByRealmId(UUID.fromString(dto.getRealmId()));
        if (realmOptional.isEmpty()) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Realm not found!", LocalDateTime.now(), null, null);
        }
        Realm realm = realmOptional.get();

        List<RealmFullAndUsernameEmailResponse> res = new ArrayList<>();
        List<User> userList = userRepository.findTop25ByUsernameStartingWithAndIdIsNotIn(dto.getUsername(), excludedIds(realm.getRealmAdmins()), Sort.by("username").ascending());
        for (User resUser : userList) {
            res.add(
                    new RealmFullAndUsernameEmailResponse(
                            resUser.getUsername(),
                            resUser.getUserDetail().getFirstName() + " " + resUser.getUserDetail().getLastName(),
                            resUser.getEmail(),
                            resUser.getUid(),
                            resUser.getUserDetail().getProfilePicture()
                    )
            );
        }

        return new ApplicationResponse<>(ResponseStatus.SUCCESS, "Users listed!", LocalDateTime.now(), null, res);
    }

    public ApplicationResponse<List<RealmFullAndUsernameEmailResponse>> getHitForUserAutoComplete(AutocompleteUserToMemberListDTO dto) {
        Optional<Realm> realmOptional = realmRepository.findByRealmId(UUID.fromString(dto.getRealmId()));
        if (realmOptional.isEmpty()) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Realm not found!", LocalDateTime.now(), null, null);
        }
        Realm realm = realmOptional.get();

        List<Long> excludedIds = new ArrayList<>(excludedIds(realm.getRealmAdmins()));
        excludedIds.addAll(excludedIds(realm.getRealmUsers()));
        List<RealmFullAndUsernameEmailResponse> res = new ArrayList<>();
        List<User> userList = userRepository.findTop25ByUsernameStartingWithAndIdIsNotIn(dto.getUsername(), excludedIds, Sort.by("username").ascending());
        for (User resUser : userList) {
            res.add(
                    new RealmFullAndUsernameEmailResponse(
                            resUser.getUsername(),
                            resUser.getUserDetail().getFirstName() + " " + resUser.getUserDetail().getLastName(),
                            resUser.getEmail(),
                            resUser.getUid(),
                            resUser.getUserDetail().getProfilePicture()
                    )
            );
        }

        return new ApplicationResponse<>(ResponseStatus.SUCCESS, "Users listed!", LocalDateTime.now(), null, res);
    }

    private boolean isRealmAdmin(User user, Realm realm) {
        boolean result = false;
        for (User admin : realm.getRealmAdmins()) {
            if (admin.getId().equals(user.getId())) {
                result = true;
                break;
            }
        }
        return result;
    }

    private boolean isRealmUser(User user, Realm realm) {
        boolean result = false;
        for (User rUser : realm.getRealmUsers()) {
            if (rUser.getId().equals(user.getId())) {
                result = true;
                break;
            }
        }
        return result;
    }

    public ApplicationResponse<List<RealmAdminResponse>> addRealmUser(User user, AddNewRealmAdminDTO dto) {
        Optional<Realm> realmOptional = realmRepository.findByRealmId(UUID.fromString(dto.getRealmId()));
        if (realmOptional.isEmpty()) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Realm not found!", LocalDateTime.now(), null, null);
        }
        Realm realm = realmOptional.get();
        if (!isRealmAdmin(user, realm)) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "You are not an admin for the selected realm!", LocalDateTime.now(), null, null);
        }

        Optional<User> userOptional = userRepository.findByUid(UUID.fromString(dto.getUid()));
        if (userOptional.isEmpty()) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "User not found!", LocalDateTime.now(), null, null);
        }

        if (isRealmAdmin(userOptional.get(), realm)) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "The user is on the admin list!", LocalDateTime.now(), null, null);
        }

        if (isRealmUser(user, realm)) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "User already has access!", LocalDateTime.now(), null, null);
        }

        realm.getRealmUsers().add(userOptional.get());
        realmRepository.save(realm);

        ApplicationResponse<List<RealmAdminResponse>> res = getOwnedRealms(user);
        if (res.getStatus() == ResponseStatus.SUCCESS) {
            res.setMessage("Realm user added successfully!");
        }

        return res;
    }

    public ApplicationResponse<List<RealmAdminResponse>> deleteRealmUser(User user, AddNewRealmAdminDTO dto) {
        Optional<Realm> realmOptional = realmRepository.findByRealmId(UUID.fromString(dto.getRealmId()));
        if (realmOptional.isEmpty()) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Realm not found!", LocalDateTime.now(), null, null);
        }
        Realm realm = realmOptional.get();

        if (!isRealmAdmin(user, realm)) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "You are not an admin for the selected realm!", LocalDateTime.now(), null, null);
        }

        Optional<User> userOptional = userRepository.findByUid(UUID.fromString(dto.getUid()));
        if (userOptional.isEmpty()) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "User not found!", LocalDateTime.now(), null, null);
        }

        if (!isRealmUser(userOptional.get(), realm)) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "The user is not on the user list!", LocalDateTime.now(), null, null);
        }

        realm.getRealmUsers().remove(userOptional.get());
        realmRepository.save(realm);

        ApplicationResponse<List<RealmAdminResponse>> res = getOwnedRealms(user);
        if (res.getStatus() == ResponseStatus.SUCCESS) {
            res.setMessage("Realm user removed successfully!");
        }

        return res;
    }

    public ApplicationResponse<List<RealmAdminResponse>> addRealmAdmin(User user, AddNewRealmAdminDTO dto) {
        Optional<Realm> realmOptional = realmRepository.findByRealmId(UUID.fromString(dto.getRealmId()));
        if (realmOptional.isEmpty()) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Realm not found!", LocalDateTime.now(), null, null);
        }
        Realm realm = realmOptional.get();

        if (!isRealmAdmin(user, realm)) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "You are not an admin for the selected realm!", LocalDateTime.now(), null, null);
        }

        Optional<User> userOptional = userRepository.findByUid(UUID.fromString(dto.getUid()));
        if (userOptional.isEmpty()) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "User not found!", LocalDateTime.now(), null, null);
        }

        if (isRealmAdmin(userOptional.get(), realm)) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "The user already has admin access!", LocalDateTime.now(), null, null);
        }

        realm.getRealmAdmins().add(userOptional.get());
        realmRepository.save(realm);

        ApplicationResponse<List<RealmAdminResponse>> res = getOwnedRealms(user);
        if (res.getStatus() == ResponseStatus.SUCCESS) {
            res.setMessage("Realm admin added successfully!");
        }

        return res;
    }

    public ApplicationResponse<List<RealmAdminResponse>> deleteRealmAdmin(User user, AddNewRealmAdminDTO dto) {
        Optional<Realm> realmOptional = realmRepository.findByRealmId(UUID.fromString(dto.getRealmId()));
        if (realmOptional.isEmpty()) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Realm not found!", LocalDateTime.now(), null, null);
        }
        Realm realm = realmOptional.get();

        if (!isRealmAdmin(user, realm)) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "You are not an admin for the selected realm!", LocalDateTime.now(), null, null);
        }

        if (realm.getRealmAdmins().size() == 1) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Every realm must have least one administrator!", LocalDateTime.now(), null, null);
        }

        Optional<User> userOptional = userRepository.findByUid(UUID.fromString(dto.getUid()));
        if (userOptional.isEmpty()) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "User not found!", LocalDateTime.now(), null, null);
        }

        if (!isRealmAdmin(userOptional.get(), realm)) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "The user is not on the list!", LocalDateTime.now(), null, null);
        }

        realm.getRealmAdmins().remove(userOptional.get());
        realmRepository.save(realm);

        ApplicationResponse<List<RealmAdminResponse>> res = getOwnedRealms(user);
        if (res.getStatus() == ResponseStatus.SUCCESS) {
            res.setMessage("Realm admin removed successfully!");
        }

        return res;
    }

    public ApplicationResponse<List<RealmAdminResponse>> delete(User user, OAuthCodeCreateDTO dto) {
        Optional<Realm> realmOptional = realmRepository.findByRealmId(UUID.fromString(dto.getRealmId()));
        if (realmOptional.isEmpty()) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Realm not found!", LocalDateTime.now(), null, null);
        }
        Realm realm = realmOptional.get();
        if (!user.getId().equals(realm.getCreatedBy().getId())) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Only the owner can delete the realm!", LocalDateTime.now(), null, null);
        }

        realmSubscriptionRepository.delete(realm.getSubscription());
        realm.setSubscription(null);
        realmRepository.delete(realm);
        ApplicationResponse<List<RealmAdminResponse>> res = getOwnedRealms(user);
        if (res.getStatus() == ResponseStatus.SUCCESS) {
            res.setMessage("Realm deleted successfully!");
        }
        return res;
    }

    public ApplicationResponse<List<RealmAdminResponse>> uploadLogo(User user, String realmId, MultipartFile file) throws IOException {
        Optional<Realm> realmOptional = realmRepository.findByRealmId(UUID.fromString(realmId));
        if (realmOptional.isEmpty()) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Realm not found!", LocalDateTime.now(), null, null);
        }
        Realm realm = realmOptional.get();
        if(!isRealmAdmin(user, realm)) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "You are not an admin for the realm!", LocalDateTime.now(), null, null);
        }

        realm.setLogo(realmImageUrl + uploadRealmLogoOnCreate(file).getImageId());
        realmRepository.save(realm);
        ApplicationResponse<List<RealmAdminResponse>> res = getOwnedRealms(user);
        if (res.getStatus() == ResponseStatus.SUCCESS) {
            res.setMessage("Logo uploaded successfully!");
        }
        return res;
    }

    public ApplicationResponse<List<RealmAdminResponse>> updateRealmProperties(User user, UpdateRealmPropertiesDTO dto) {
        Optional<Realm> realmOptional = realmRepository.findByRealmId(UUID.fromString(dto.getRealmId()));
        if (realmOptional.isEmpty()) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "Realm not found!", LocalDateTime.now(), null, null);
        }
        Realm realm = realmOptional.get();
        if(!isRealmAdmin(user, realm)) {
            return new ApplicationResponse<>(ResponseStatus.ERROR, "You are not an admin for the realm!", LocalDateTime.now(), null, null);
        }

        realm.setDisplayName(dto.getName());
        realm.setOrigin(dto.getOrigin());
        realm.setRedirectURI(dto.getRedirectURI());
        realm.setHelperEmail(dto.getHelperEmail());
        realm.setPubliclyAvailable(dto.isPubliclyAvailable());
        realm.setDisabled(dto.isDisabled());
        realm.setLastModifiedDate(LocalDateTime.now());
        realmRepository.save(realm);
        ApplicationResponse<List<RealmAdminResponse>> res = getOwnedRealms(user);
        if (res.getStatus() == ResponseStatus.SUCCESS) {
            res.setMessage("Realm updated successfully!");
        }
        return res;
    }
}
