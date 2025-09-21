package com.vzt.api.dtos.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsernamePasswordDTO {
    private String username;
    private String password;
    private String realmId;
}
