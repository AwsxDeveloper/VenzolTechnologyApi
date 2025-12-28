package com.vzt.api.models.realm;

import com.vzt.api.models.BillingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "realm_plan_subscriptions")
public class RealmSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, updatable = false)
    private UUID subscriptionId;
    @Enumerated(EnumType.STRING)
    private RealmPlan subscriptionPlan;
    @Enumerated(EnumType.STRING)
    private BillingStatus billingStatus;
    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;
    private LocalDateTime renewedAt;
    private LocalDateTime endAt;
}
