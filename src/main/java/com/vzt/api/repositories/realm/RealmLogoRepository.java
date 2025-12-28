package com.vzt.api.repositories.realm;

import com.vzt.api.models.realm.RealmLogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface RealmLogoRepository extends JpaRepository<RealmLogo, Long> {
    boolean existsByImageId(UUID imageId);


    Optional<RealmLogo> findByImageId(UUID imageId);
}
