package com.vzt.api.responses.realm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallBackResponse {
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String userId;
    private String country;
    private String language;
    private String gender;
    private LocalDate dateOfBirth;
    private String profilePicture;
}
