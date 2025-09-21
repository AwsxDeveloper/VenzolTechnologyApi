package com.vzt.api.controllers.account;

import com.vzt.api.dtos.account.ChangePasswordDTO;
import com.vzt.api.dtos.account.UpdateProfileDTO;
import com.vzt.api.models.authentication.ProfilePicture;
import com.vzt.api.responses.ApplicationResponse;
import com.vzt.api.responses.account.LoggedInUsersResponse;
import com.vzt.api.responses.account.ProfileResponse;
import com.vzt.api.services.account.AccountService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/account")
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/logged-in")
    public ResponseEntity<ApplicationResponse<List<LoggedInUsersResponse>>> getLoggedInUsers(HttpServletRequest request) {
        ApplicationResponse<List<LoggedInUsersResponse>> response = accountService.getLoggedInUsers(request);
        return new ResponseEntity<>(response, response.getStatus().value);
    }

    @GetMapping("/profile")
    public ResponseEntity<ApplicationResponse<ProfileResponse>> getProfile(HttpServletRequest request) {
        ApplicationResponse<ProfileResponse> response = accountService.getProfileData(request);
        return new ResponseEntity<>(response, response.getStatus().value);
    }

    @PutMapping("/profile")
    public ResponseEntity<ApplicationResponse<ProfileResponse>> updateProfile(HttpServletRequest request, @RequestBody UpdateProfileDTO dto) {
        ApplicationResponse<ProfileResponse> response = accountService.updateProfileData(request, dto);
        return new ResponseEntity<>(response, response.getStatus().value);
    }

    @GetMapping("/profile-picture/{imageId}")
    public ResponseEntity<byte[]> getProfilePicture(@PathVariable String imageId) throws IOException {
        ProfilePicture img = accountService.getProfilePicture(imageId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(img.getContentType()));
        return new ResponseEntity<>(img.getImage(), headers, HttpStatus.OK);
    }

    @PostMapping("/profile-picture")
    public ResponseEntity<ApplicationResponse<?>> uploadProfilePicture(HttpServletRequest request, @RequestParam("file") MultipartFile file) throws IOException {
        ApplicationResponse<?> res = accountService.uploadProfilePicture(request, file);
        return new ResponseEntity<>(res, res.getStatus().value);
    }

    @DeleteMapping("/profile-picture")
    public ResponseEntity<ApplicationResponse<?>> deleteProfilePicture(HttpServletRequest request) {
        ApplicationResponse<?> res = accountService.deleteProfilePicture(request);
        return new ResponseEntity<>(res, res.getStatus().value);
    }

    @PutMapping("password")
    public ResponseEntity<ApplicationResponse> changePassword(HttpServletRequest request, @RequestBody ChangePasswordDTO dto) {
        ApplicationResponse<?> res = accountService.changePassword(request, dto);
        return new ResponseEntity<>(res, res.getStatus().value);
    }

}
