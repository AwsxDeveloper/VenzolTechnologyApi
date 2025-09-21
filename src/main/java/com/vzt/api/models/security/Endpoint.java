package com.vzt.api.models.security;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Data
@Entity(name = "endpoints")
@NoArgsConstructor
@AllArgsConstructor
public class Endpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String path;

    @Enumerated(EnumType.STRING)
    private HttpMethod method;

    private boolean disabled;

    private boolean usableForOAuth;

    @Lob
    private String description;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "permission_id", referencedColumnName = "id")
    private Permission permission;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
