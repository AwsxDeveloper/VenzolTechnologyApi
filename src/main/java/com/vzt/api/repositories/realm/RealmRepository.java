package com.vzt.api.repositories.realm;

import com.vzt.api.models.authentication.User;
import com.vzt.api.models.realm.Realm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RealmRepository extends JpaRepository<Realm, Long> {
    boolean existsByRealmId(UUID realmId);
    boolean existsBySecret(String secret);
    Optional<Realm> findByRealmId(UUID realmId);
    boolean existsByOriginAndDisabledIsFalse(String origin);
    List<Realm> findAllByRealmAdminsContains(User user);
}