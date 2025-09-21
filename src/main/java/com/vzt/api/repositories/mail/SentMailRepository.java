package com.vzt.api.repositories.mail;

import com.vzt.api.models.mail.SentMail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SentMailRepository extends JpaRepository<SentMail, Long> {
}
