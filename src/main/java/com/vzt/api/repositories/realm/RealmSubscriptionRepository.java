package com.vzt.api.repositories.realm;

import com.vzt.api.models.realm.RealmSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RealmSubscriptionRepository extends JpaRepository<RealmSubscription, Long> {
    boolean existsBySubscriptionId(UUID subscriptionId);
}
