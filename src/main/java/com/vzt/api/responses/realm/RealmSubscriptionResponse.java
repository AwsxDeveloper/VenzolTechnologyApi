package com.vzt.api.responses.realm;

import com.vzt.api.models.BillingStatus;
import com.vzt.api.models.realm.RealmPlan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RealmSubscriptionResponse {
    private UUID subscriptionId;
    private RealmPlan realmPlan;
    private BillingStatus billingStatus;
    private LocalDateTime createdAt;
    private LocalDateTime renewedAt;
    private LocalDateTime endAt;
}
