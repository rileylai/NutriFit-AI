package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.OneToOne;
import jakarta.persistence.FetchType;

import java.time.Instant;
import java.util.UUID;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "uuid", unique = true, nullable = false,
            updatable = false, columnDefinition = "UUID")
    private UUID uuid;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "password_hash")
    private String passwordHash;

    @Transient
    private String password;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @Column(name = "reset_password_token_expires_at")
    private Instant resetPasswordTokenExpiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private Instant updatedAt;

    // Relationships to other entities can be added here as needed:
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<NutritionTarget> nutritionTargets = new ArrayList<>();

    // Optional 1:1 link to user profile; kept LAZY to avoid loading PII during auth flows
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = true)
    private UserProfile profile;

    // Helper method to get current active nutrition target
    public NutritionTarget getCurrentTargets() {
        return nutritionTargets.stream()
            .filter(NutritionTarget::getIsActive)
            .filter(target -> target.getEndDate() == null ||
                             target.getEndDate().isAfter(LocalDate.now()))
            .findFirst()
            .orElse(null);
    }

}
