package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMetrics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "metric_id")
    private Long metricId;
    
    // Relationship: users can have multiple metric entries over time
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "height_cm", precision = 5, scale = 2)
    private BigDecimal heightCm;
    
    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;
    
    @Column(name = "age")
    private Integer age;
    
    @Column(name = "gender")
    private String gender;
    
    @Column(name = "bmr", precision = 8, scale = 2)
    private BigDecimal bmr;
    
    @Column(name = "bmi", precision = 8, scale = 2)
    private BigDecimal bmi;
    
    @Column(name = "user_goal")
    private String userGoal;
    
    @Column(name = "record_at")
    private LocalDateTime recordAt;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Helper methods
    public Double getWeight() {
        return weightKg != null ? weightKg.doubleValue() : 0.0;
    }
    
    public Double getHeight() {
        return heightCm != null ? heightCm.doubleValue() : 0.0;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdAt;
    }
    
    public void setWeight(Double weight) {
        this.weightKg = weight != null ? BigDecimal.valueOf(weight) : null;
    }
    
    public void setHeight(Double height) {
        this.heightCm = height != null ? BigDecimal.valueOf(height) : null;
    }
    
    public Long getUserId() {
        return user != null ? user.getUserId() : null;
    }
}
