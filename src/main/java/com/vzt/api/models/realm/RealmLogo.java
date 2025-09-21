package com.vzt.api.models.realm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "realm_logos")
public class RealmLogo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false, updatable = false)
    private UUID imageId;
    private String contentType;
    @Lob
    @Column(columnDefinition = "VARBINARY(MAX)")
    private byte[] image;
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
}
