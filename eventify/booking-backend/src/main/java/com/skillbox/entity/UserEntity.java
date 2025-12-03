package com.skillbox.entity;

import com.skillbox.web.dto.user.UserCredentialRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static com.skillbox.entity.RoleType.ROLE_USER;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "app_user")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String email;

    private String password;

    @ElementCollection(targetClass = RoleType.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<RoleType> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BookingEntity> bookings;

    @Embedded
    private NotificationSettings notificationSettings = new NotificationSettings();

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public void initialize(UserCredentialRequest request, PasswordEncoder encoder){
        email = request.getEmail();
        password = encoder.encode(request.getPassword());
        roles = Set.of(ROLE_USER);
        bookings = new HashSet<>();
        notificationSettings = new NotificationSettings();
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
}
