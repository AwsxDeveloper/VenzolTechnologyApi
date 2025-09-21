package com.vzt.api.dtos.realm;

import com.vzt.api.models.realm.RealmPlan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateRealmDTO {
    private String name;
    private String helperEmail;
    private String origin;
    private String redirectURL;
    private RealmPlan plan;
    private boolean publiclyAvailable;
}

