package com.vzt.api.models.realm;

import com.vzt.api.models.authentication.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "realms")
public class Realm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false, updatable = false)
    private UUID realmId;
    private String displayName;
    @Column(unique = true, nullable = false, updatable = false)
    private String secret;
    private String logo;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "realm_admins",
            joinColumns = @JoinColumn(name = "realm_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> realmAdmins;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    private User CreatedBy;
    private String origin;
    private String redirectURI;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "subscription_id", referencedColumnName = "id")
    private RealmSubscription subscription;
    private boolean disabled;
    private boolean publiclyAvailable;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "realm_users",
            joinColumns = @JoinColumn(name = "realm_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> realmUsers;
    private String helperEmail;
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;
}
