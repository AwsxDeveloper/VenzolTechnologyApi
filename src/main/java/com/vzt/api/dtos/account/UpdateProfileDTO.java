package com.vzt.api.dtos.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String gender;
    private String country;
    private String language;
}

