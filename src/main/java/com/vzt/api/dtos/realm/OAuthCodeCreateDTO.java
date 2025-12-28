package com.vzt.api.dtos.realm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuthCodeCreateDTO {
    private String realmId;
}
