package com.vzt.api.responses.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoggedInUsersResponse {
    private int activeUserId;
    private String fullName;
    private String profilePicture;
    private String accessToken;
}
