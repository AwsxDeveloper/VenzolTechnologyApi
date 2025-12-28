package com.vzt.api.responses.realm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RealmFullAndUsernameEmailResponse {
    private String username;
    private String fullName;
    private String email;
    private UUID uid;
    private String profilePicture;
}
