package com.vzt.api.models.authentication;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "mfa_settings")
public class MFASetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String secret;
    private String deletionKey;
    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime setAt;
}
