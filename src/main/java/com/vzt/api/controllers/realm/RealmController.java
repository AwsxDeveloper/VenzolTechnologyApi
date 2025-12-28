package com.vzt.api.controllers.realm;

import com.vzt.api.dtos.realm.*;
import com.vzt.api.models.authentication.User;
import com.vzt.api.models.realm.RealmLogo;
import com.vzt.api.models.realm.RealmPlan;
import com.vzt.api.responses.ApplicationResponse;
import com.vzt.api.responses.realm.RealmAdminResponse;
import com.vzt.api.responses.realm.RealmFullAndUsernameEmailResponse;
import com.vzt.api.responses.realm.RealmResponse;
import com.vzt.api.services.realm.RealmService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/realm")
public class RealmController {
    private final RealmService realmService;

    @PostMapping("/create")
    public ResponseEntity<ApplicationResponse<List<RealmAdminResponse>>> create(
            @AuthenticationPrincipal User user,
            @RequestParam("displayName") String displayName,
            @RequestParam("origin") String origin,
            @RequestParam("redirectURI") String redirectURI,
            @RequestParam("helperEmail") String helperEmail,
            @RequestParam("file") MultipartFile multipartFile,
            @RequestParam("plan") String plan,
            @RequestParam("publiclyAvailable") boolean publiclyAvailable
    ) throws IOException {
        ApplicationResponse<List<RealmAdminResponse>> response =
                realmService.create(user, new CreateRealmDTO(
                                displayName,
                                helperEmail,
                                origin,
                                redirectURI,
                                RealmPlan.valueOf(plan),
                                publiclyAvailable
                        ),
                        multipartFile
                );
        return ResponseEntity.status(response.getStatus().value).body(response);
    }

    @GetMapping("/logo/{imageId}")
    public ResponseEntity<byte[]> getProfilePicture(@PathVariable String imageId) throws IOException {
        RealmLogo img = realmService.getRealmLogo(imageId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(img.getContentType()));
        return new ResponseEntity<>(img.getImage(), headers, HttpStatus.OK);
    }

    @GetMapping("/{realm_id}")
    public ResponseEntity<ApplicationResponse<RealmResponse>> getRealmById(
            @PathVariable String realm_id
    ) {
        ApplicationResponse<RealmResponse> response = realmService.getRealmById(UUID.fromString(realm_id));
        return ResponseEntity.status(response.getStatus().value).body(response);
    }

    @GetMapping("/owned")
    public ResponseEntity<ApplicationResponse<List<RealmAdminResponse>>> getOwnedRealms(@AuthenticationPrincipal User user) {
        ApplicationResponse<List<RealmAdminResponse>> response = realmService.getOwnedRealms(user);
        return ResponseEntity.status(response.getStatus().value).body(response);
    }

    @PostMapping("/hits/admin")
    public ResponseEntity<ApplicationResponse<List<RealmFullAndUsernameEmailResponse>>> hitAdmin(@RequestBody AutocompleteUserToMemberListDTO dto){
        ApplicationResponse<List<RealmFullAndUsernameEmailResponse>> response = realmService.getHitForAdminAutoComplete(dto);
        return ResponseEntity.status(response.getStatus().value).body(response);
    }

    @PostMapping("/hits/user")
    public ResponseEntity<ApplicationResponse<List<RealmFullAndUsernameEmailResponse>>> hitUser(@RequestBody AutocompleteUserToMemberListDTO dto){
        ApplicationResponse<List<RealmFullAndUsernameEmailResponse>> response = realmService.getHitForUserAutoComplete(dto);
        return ResponseEntity.status(response.getStatus().value).body(response);
    }

    @PutMapping("/admin/add")
    public ResponseEntity<ApplicationResponse<List<RealmAdminResponse>>> addNewRealmAdmin(@RequestBody AddNewRealmAdminDTO dto, @AuthenticationPrincipal User user) {
        ApplicationResponse<List<RealmAdminResponse>> response = realmService.addRealmAdmin(user, dto);
        return ResponseEntity.status(response.getStatus().value).body(response);
    }

    @PutMapping("/admin/delete")
    public ResponseEntity<ApplicationResponse<List<RealmAdminResponse>>> removeRealmAdmin(@RequestBody AddNewRealmAdminDTO dto, @AuthenticationPrincipal User user) {
        ApplicationResponse<List<RealmAdminResponse>> response = realmService.deleteRealmAdmin(user, dto);
        return ResponseEntity.status(response.getStatus().value).body(response);
    }

    @PutMapping("/user/add")
    public ResponseEntity<ApplicationResponse<List<RealmAdminResponse>>> addNewRealmUser(@RequestBody AddNewRealmAdminDTO dto, @AuthenticationPrincipal User user) {
        ApplicationResponse<List<RealmAdminResponse>> response = realmService.addRealmUser(user, dto);
        return ResponseEntity.status(response.getStatus().value).body(response);
    }

    @PutMapping("/user/delete")
    public ResponseEntity<ApplicationResponse<List<RealmAdminResponse>>> removeRealmUser(@RequestBody AddNewRealmAdminDTO dto, @AuthenticationPrincipal User user) {
        ApplicationResponse<List<RealmAdminResponse>> response = realmService.deleteRealmUser(user, dto);
        return ResponseEntity.status(response.getStatus().value).body(response);
    }

    @PutMapping("/properties")
    public ResponseEntity<ApplicationResponse<List<RealmAdminResponse>>> updateRealmProperties(@AuthenticationPrincipal User user, @RequestBody UpdateRealmPropertiesDTO dto) {
        ApplicationResponse<List<RealmAdminResponse>> response = realmService.updateRealmProperties(user, dto);
        return ResponseEntity.status(response.getStatus().value).body(response);
    }

    @PutMapping("/logo")
    public ResponseEntity<ApplicationResponse<List<RealmAdminResponse>>> uploadLogo(@AuthenticationPrincipal User user, @RequestParam("file")  MultipartFile file, @RequestParam("realmId") String realmId) throws IOException {
        ApplicationResponse<List<RealmAdminResponse>> response = realmService.uploadLogo(user, realmId, file);
        return ResponseEntity.status(response.getStatus().value).body(response);
    }

    @PostMapping("/delete")
    public ResponseEntity<ApplicationResponse<List<RealmAdminResponse>>> delete(@AuthenticationPrincipal User user, @RequestBody OAuthCodeCreateDTO dto) {
        ApplicationResponse<List<RealmAdminResponse>> response = realmService.delete(user, dto);
        return ResponseEntity.status(response.getStatus().value).body(response);
    }

}
