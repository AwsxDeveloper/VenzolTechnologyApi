package com.vzt.api.dtos.realm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRealmPropertiesDTO {
    private String realmId;
    private String name;
    private String origin;
    private String redirectURI;
    private String helperEmail;
    private boolean publiclyAvailable;
    private boolean disabled;
}
