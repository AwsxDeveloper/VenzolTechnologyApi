package com.vzt.api.responses.realm;

import com.vzt.api.models.authentication.User;
import com.vzt.api.models.realm.RealmPlan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RealmAdminResponse {
    private String realmId;
    private String name;
    private String secret;
    private String logo;
    private RealmFullAndUsernameEmailResponse createdBy;
    private List<RealmFullAndUsernameEmailResponse> admins;
    private List<RealmFullAndUsernameEmailResponse> users;
    private String origin;
    private String redirectURI;
    private String helperEmail;
    private RealmSubscriptionResponse subscription;
    private boolean publiclyAvailable;
    private boolean disabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
