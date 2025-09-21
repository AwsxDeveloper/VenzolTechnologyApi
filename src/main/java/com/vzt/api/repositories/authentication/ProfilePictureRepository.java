package com.vzt.api.repositories.authentication;

import com.vzt.api.models.authentication.ProfilePicture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfilePictureRepository extends JpaRepository<ProfilePicture, Long> {

    boolean existsByImageId(UUID imageId);
    Optional<ProfilePicture> findByImageId(UUID imageId);
}
