package com.vzt.api.repositories.authentication;

import com.vzt.api.models.authentication.MailCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailCodeRepository extends JpaRepository<MailCode, Long> {

    boolean existsByCode(String code);
}
