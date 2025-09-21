package com.vzt.api.models.session;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "browser_sessions")
public class BrowserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, unique = true, updatable = false)
    private UUID sessionId;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name="session_user_id", referencedColumnName = "id")
    private List<SessionLogin> logins;

    @ElementCollection
    @CollectionTable(name = "trusted_users", joinColumns = @JoinColumn(name = "id"))
    private List<UUID> trustedUsers;

    private String ipAddress;
    private String userAgent;


    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
