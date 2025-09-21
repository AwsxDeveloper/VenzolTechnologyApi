package com.vzt.api.models.mail;

import com.vzt.api.models.authentication.MailStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "sent_mails")
public class SentMail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String subject;
    @Enumerated(EnumType.STRING)
    private MailStatus status;

    @CreatedDate
    @Column(updatable = false,  nullable = false)
    private LocalDateTime sentAt;
}
