package com.vzt.api.responses.realm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RealmResponse {
    private String logo;
    private String name;
    private String helperEmail;
    private String redirectURL;
}
