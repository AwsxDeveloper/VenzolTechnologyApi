package com.vzt.api.responses.realm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RealmFullAndUsernameEmailResponse {
    private String username;
    private String fullName;
    private String email;
}
