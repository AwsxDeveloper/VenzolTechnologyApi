package com.vzt.api.responses.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsForLoginResponse {
    private String profilePicture;
    private String fullName;
    private String username;
    private int activeUserId;
}
