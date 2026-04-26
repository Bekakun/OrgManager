package kz.masku.orgmanager.model.entity;

import jakarta.persistence.*;
import kz.masku.orgmanager.model.enums.Role;
import kz.masku.orgmanager.model.enums.UserStatus;
import lombok.*;

/**
 * Employee / system user.
 * Maps to the {@code users} table.
 * Password is stored only as a BCrypt hash — never in plaintext.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "position")
    private String position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /** BCrypt-hashed password. Never expose in API responses. */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;
}
