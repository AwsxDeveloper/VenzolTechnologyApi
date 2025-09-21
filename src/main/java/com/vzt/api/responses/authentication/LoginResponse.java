package com.vzt.api.responses.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private boolean mfaRequired;
    private boolean credentialsExpired;
    private int activeUserId;
}
