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
@Entity(name = "bad_attempts")
public class BadAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private BadAttemptName badAttemptName;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime timestamp;
}
