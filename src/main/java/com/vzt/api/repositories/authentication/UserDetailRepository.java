package com.vzt.api.repositories.authentication;

import com.vzt.api.models.authentication.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDetailRepository extends JpaRepository<UserDetail, Long> {

    boolean existsByPhoneNumber(String phoneNumber);
}
