package com.vzt.api.responses.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String phoneNumber;
    private LocalDate birthday;
    private String gender;
    private String country;
    private String language;
    private LocalDateTime joinDate;
    private LocalDateTime lastUpdate;
}
