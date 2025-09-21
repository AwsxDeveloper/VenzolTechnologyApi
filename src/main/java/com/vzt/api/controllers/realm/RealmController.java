package com.vzt.api.controllers.realm;

import com.vzt.api.dtos.realm.CreateRealmDTO;
import com.vzt.api.models.realm.RealmLogo;
import com.vzt.api.models.realm.RealmPlan;
import com.vzt.api.responses.ApplicationResponse;
import com.vzt.api.responses.realm.RealmAdminResponse;
import com.vzt.api.responses.realm.RealmResponse;
import com.vzt.api.services.realm.RealmService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/realm")
public class RealmController {
    private final RealmService realmService;

    @PostMapping("/create")
    public ResponseEntity<ApplicationResponse<List<RealmAdminResponse>>> create(
            HttpServletRequest request,
            @RequestParam("name") String name,
            @RequestParam("origin") String origin,
            @RequestParam("redirectURI") String redirectURI,
            @RequestParam("helperEmail") String helperEmail,
            @RequestParam("logo") MultipartFile multipartFile,
            @RequestParam("plan") String plan,
            @RequestParam("publiclyAvailable") boolean publiclyAvailable
    ) throws IOException {
        ApplicationResponse<List<RealmAdminResponse>> response =
                realmService.create(request, new CreateRealmDTO(
                                name,
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
    public ResponseEntity<ApplicationResponse<List<RealmAdminResponse>>> getOwnedRealms(HttpServletRequest request) {
        ApplicationResponse<List<RealmAdminResponse>> response = realmService.getOwnedRealms(request);
        return ResponseEntity.status(response.getStatus().value).body(response);
    }

}
