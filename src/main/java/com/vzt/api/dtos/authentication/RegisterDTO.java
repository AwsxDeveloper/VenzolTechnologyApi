package com.vzt.api.dtos.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO {
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String language;
    private String country;
    private String gender;
    private LocalDate birthDate;
}
